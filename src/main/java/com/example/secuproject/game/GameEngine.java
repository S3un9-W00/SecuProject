package com.example.secuproject.game;

import com.example.secuproject.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 게임의 핵심 로직을 처리하는 클래스
 * 플레이어 이동, 안개 시스템, 아이템 등을 관리합니다
 */
public class GameEngine {
    private Maze maze;
    private Player playerMe; // 나 (플레이어 1)
    private Player playerYou; // 너 (플레이어 2)
    private long gameStartTime;
    private List<String> gameLog; // 게임 로그 (조작 내용 기록)
    
    public GameEngine(Maze maze) {
        this.maze = maze;
        this.gameLog = new ArrayList<>();
        this.gameStartTime = System.currentTimeMillis();
        
        // 스타트 지점에 플레이어 배치
        List<Position> starts = maze.getStartPositions();
        Position startMe = null;
        Position startYou = null;
        
        if (starts.size() >= 2) {
            // 이동 가능한 첫 번째 스타트 지점 찾기
            for (Position start : starts) {
                if (maze.isWalkable(start)) {
                    if (startMe == null) {
                        startMe = start;
                    } else if (startYou == null) {
                        startYou = start;
                        break;
                    }
                }
            }
        } else if (starts.size() == 1) {
            // 이동 가능한 스타트 지점 찾기
            for (Position start : starts) {
                if (maze.isWalkable(start)) {
                    startMe = start;
                    break;
                }
            }
        }
        
        // 이동 가능한 스타트 지점이 없으면 찾기
        if (startMe == null) {
            startMe = findWalkablePosition(maze);
        }
        if (startYou == null) {
            startYou = findWalkablePosition(maze, startMe);
        }
        
        playerMe = new Player(Player.PlayerType.ME, startMe);
        playerYou = new Player(Player.PlayerType.YOU, startYou);
        
        // 초기 가시성 업데이트
        updateVisibility();
    }
    
    public Maze getMaze() {
        return maze;
    }
    
    public Player getPlayerMe() {
        return playerMe;
    }
    
    public Player getPlayerYou() {
        return playerYou;
    }
    
    public List<String> getGameLog() {
        return new ArrayList<>(gameLog);
    }
    
    /**
     * 플레이어를 이동시킵니다
     * @param player 이동할 플레이어
     * @param direction 이동 방향 (UP, DOWN, LEFT, RIGHT)
     * @return 이동 성공 여부
     */
    public boolean movePlayer(Player player, Direction direction) {
        Position currentPos = player.getPosition();
        Position newPos = calculateNewPosition(currentPos, direction);
        
        // 이동 가능한지 확인
        if (!maze.isValidPosition(newPos) || !maze.isWalkable(newPos)) {
            return false;
        }
        
        // AI와는 통과 가능 (체크 제거)
        
        // 이동 실행
        player.setPosition(newPos);
        
        // 아이템 확인
        MazeCell cell = maze.getCell(newPos);
        if (cell.hasItem()) {
            player.activateItem();
            maze.setCell(newPos, 3); // 아이템을 먹으면 길로 변경
            logAction(player, "아이템을 획득했습니다! (5×5 스캔 활성화)");
        }
        
        // 도착지점 확인 (위치와 셀 코드 모두 확인)
        Position goalPosition = maze.getGoalPosition();
        MazeCell currentCell = maze.getCell(newPos);
        boolean isGoal = (goalPosition != null && newPos.equals(goalPosition)) || 
                         (currentCell != null && currentCell.getCode() == 9);
        
        if (isGoal) {
            long elapsedTime = System.currentTimeMillis() - gameStartTime;
            player.setReachedGoal(true);
            player.setFinishTime(elapsedTime);
            logAction(player, "도착지점에 도달했습니다! (소요 시간: " + (elapsedTime / 1000) + "초)");
            
            // 상대 플레이어에게 알림 전송
            Player opponent = (player == playerMe) ? playerYou : playerMe;
            logAction(opponent, "상대방이 도착지점에 도달했습니다!");
        }
        
        // 가시성 업데이트
        updateVisibility();
        
        // 로그 기록
        logAction(player, direction + " 방향으로 이동");
        
        return true;
    }
    
    /**
     * 이동 방향에 따른 새 위치를 계산합니다
     */
    private Position calculateNewPosition(Position current, Direction direction) {
        int x = current.getX();
        int y = current.getY();
        
        switch (direction) {
            case UP:
                x--;
                break;
            case DOWN:
                x++;
                break;
            case LEFT:
                y--;
                break;
            case RIGHT:
                y++;
                break;
        }
        
        return new Position(x, y);
    }
    
    /**
     * 안개 시스템: 플레이어 주변만 가시화합니다
     */
    private void updateVisibility() {
        // 양쪽 플레이어의 가시성을 합쳐서 표시
        List<Position> positions = new ArrayList<>();
        positions.add(playerMe.getPosition());
        positions.add(playerYou.getPosition());
        
        List<Boolean> hasItems = new ArrayList<>();
        hasItems.add(playerMe.hasItem());
        hasItems.add(playerYou.hasItem());
        
        maze.updateVisibilityForAll(positions, hasItems);
    }
    
    /**
     * 게임 액션을 로그에 기록합니다
     */
    private void logAction(Player player, String action) {
        String playerName = (player.getType() == Player.PlayerType.ME) ? "나" : "너";
        long elapsedTime = System.currentTimeMillis() - gameStartTime;
        String logEntry = String.format("[%.1f초] %s: %s", elapsedTime / 1000.0, playerName, action);
        gameLog.add(logEntry);
    }
    
    /**
     * 게임이 종료되었는지 확인합니다 (플레이어(나)가 도착하면 게임 종료)
     */
    public boolean isGameFinished() {
        return playerMe.isReachedGoal();
    }
    
    /**
     * 이동 가능한 위치를 찾습니다
     */
    private Position findWalkablePosition(Maze maze) {
        return findWalkablePosition(maze, null);
    }
    
    /**
     * 이동 가능한 위치를 찾습니다 (다른 위치와 다른 위치)
     */
    private Position findWalkablePosition(Maze maze, Position exclude) {
        int size = maze.getSize();
        for (int i = 1; i < size - 1; i++) {
            for (int j = 1; j < size - 1; j++) {
                Position pos = new Position(i, j);
                if (maze.isWalkable(pos) && (exclude == null || !pos.equals(exclude))) {
                    return pos;
                }
            }
        }
        // 찾지 못하면 (1, 1) 반환
        return new Position(1, 1);
    }
    
    /**
     * 이동 방향 열거형
     */
    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }
}

