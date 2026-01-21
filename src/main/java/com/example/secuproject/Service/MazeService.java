package com.example.secuproject.Service;

import com.example.secuproject.Maze_two;
import com.example.secuproject.Enemy;
import com.example.secuproject.util.MazeGenerator;
import com.example.secuproject.util.MazeValidator;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 간단한 미로 게임 서비스
 * Maze_two와 Enemy만 사용하여 게임을 관리합니다
 */
@Service
public class MazeService {
    private Maze_two maze;
    private Enemy enemy; // AI 로봇 (오른손 법칙 사용)
    private boolean gameStarted = false;
    private boolean gameFinished = false;
    private MazeGenerator generator;
    private MazeValidator validator;
    private final SecureRandom random = new SecureRandom();

    public MazeService() {
        this.generator = new MazeGenerator();
        this.validator = new MazeValidator();
    }

    /**
     * 게임 시작 - txt 파일에서 미로 읽기
     */
    public void startGame() {
        startGame("maze.txt"); // 기본 파일명
    }
    
    /**
     * 게임 시작 - 지정된 파일에서 미로 읽기
     */
    public void startGame(String filePath) {
        try {
            // 파일에서 미로 읽기
            maze = Maze_two.fromFile(filePath);
            
            // 미로 검증
            MazeValidator.ValidationResult result = validator.validate(maze.getMap());
            if (!result.valid) {
                System.out.println("미로 검증 실패: " + result.message);
                // 검증 실패 시 기본 미로 사용
                maze = new Maze_two();
            }
        } catch (IOException e) {
            System.out.println("파일 읽기 실패: " + e.getMessage());
            // 파일 읽기 실패 시 기본 미로 사용
            maze = new Maze_two();
        }

        // 외부 맵에 아이템/함정이 없을 때 기본 배치 (스프링 플레이용)
        ensureObjectIfMissing(maze.getMap(), 6, 1); // 횃불
        ensureObjectIfMissing(maze.getMap(), 7, 1); // 망치
        ensureObjectIfMissing(maze.getMap(), 8, 1); // 함정
        
        // Enemy는 플레이어와 다른 스타트 지점에서 시작
        int enemyStartX = findEnemyStartX();
        int enemyStartY = findEnemyStartY();
        enemy = new Enemy(maze, enemyStartX, enemyStartY, maze.getMap());
        gameStarted = true;
        gameFinished = false;
    }

    /**
     * Enemy를 위한 시작 위치 찾기 (플레이어와 다른 이동 가능한 위치)
     */
    private int findEnemyStartX() {
        int playerX = maze.getPlayerX();
        int playerY = maze.getPlayerY();
        int[][] map = maze.getMap();
        int size = maze.getSize();
        
        // 플레이어와 다른 스타트 지점 찾기
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (map[i][j] == 0 && (i != playerX || j != playerY)) {
                    return i;
                }
            }
        }
        
        // 스타트 지점이 하나면 플레이어 근처의 이동 가능한 위치 찾기
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (map[i][j] != 4 && map[i][j] != 9 && (i != playerX || j != playerY)) {
                    return i;
                }
            }
        }
        
        // 기본값: 플레이어 위치에서 1칸 떨어진 곳
        return Math.min(playerX + 1, size - 1);
    }

    private int findEnemyStartY() {
        int playerX = maze.getPlayerX();
        int playerY = maze.getPlayerY();
        int[][] map = maze.getMap();
        int size = maze.getSize();
        
        // 플레이어와 다른 스타트 지점 찾기
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (map[i][j] == 0 && (i != playerX || j != playerY)) {
                    return j;
                }
            }
        }
        
        // 스타트 지점이 하나면 플레이어 근처의 이동 가능한 위치 찾기
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (map[i][j] != 4 && map[i][j] != 9 && (i != playerX || j != playerY)) {
                    return j;
                }
            }
        }
        
        // 기본값: 플레이어 위치에서 1칸 떨어진 곳
        return Math.min(playerY + 1, size - 1);
    }

    /**
     * 미로 화면 보기 (Enemy 위치 포함)
     */
    public String getView() {
        if (maze == null) {
            return "게임을 시작해주세요.";
        }
        if (enemy != null) {
            return maze.showMaze(enemy.getX(), enemy.getY());
        }
        return maze.showMaze();
    }

    /**
     * 플레이어 이동 (w/a/s/d)
     */
    public Maze_two.MoveResult move(char dir) {
        if (maze == null) {
            return new Maze_two.MoveResult(false, false, "게임을 시작해주세요.");
        }
        
        Maze_two.MoveResult result = maze.move(dir);
        
        // 도착했는지 확인
        if (result.arrived) {
            gameFinished = true;
        }
        
        return result;
    }

    /**
     * AI(Enemy) 이동 - 오른손 법칙으로 자동 이동
     */
    public void aiMove() {
        if (enemy != null && !gameFinished) {
            enemy.step();
            
            // Enemy가 도착지점에 도달했는지 확인
            int[][] map = maze.getMap();
            int ex = enemy.getX();
            int ey = enemy.getY();
            if (ex >= 0 && ex < map.length && ey >= 0 && ey < map[0].length) {
                if (map[ex][ey] == 9) {
                    gameFinished = true;
                }
            }
        }
    }

    /**
     * 게임 리셋
     */
    public void reset() {
        startGame();
    }

    /**
     * 게임 상태 정보 가져오기
     */
    public GameStatus getStatus() {
        GameStatus status = new GameStatus();
        status.mazeView = getView();
        status.playerX = maze != null ? maze.getPlayerX() : -1;
        status.playerY = maze != null ? maze.getPlayerY() : -1;
        status.enemyX = enemy != null ? enemy.getX() : -1;
        status.enemyY = enemy != null ? enemy.getY() : -1;
        status.gameStarted = gameStarted;
        status.gameFinished = gameFinished;
        return status;
    }

    /**
     * 맵에 지정된 코드가 최소 count개 있도록 비어있는 길(3)에 추가합니다.
     * 외부 txt 맵에 아이템/함정이 없을 때도 웹 플레이가 심심하지 않도록 보강.
     */
    private void ensureObjectIfMissing(int[][] map, int code, int count) {
        int existing = 0;
        for (int[] row : map) {
            for (int v : row) {
                if (v == code) existing++;
            }
        }
        if (existing >= count) return;

        List<int[]> candidates = new ArrayList<>();
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                if (map[i][j] == 3) { // 길에만 배치
                    candidates.add(new int[]{i, j});
                }
            }
        }
        Collections.shuffle(candidates, random);
        int toPlace = Math.min(count - existing, candidates.size());
        for (int k = 0; k < toPlace; k++) {
            int[] p = candidates.get(k);
            map[p[0]][p[1]] = code;
        }
    }

    /**
     * 게임 상태 클래스
     */
    public static class GameStatus {
        public String mazeView;
        public int playerX;
        public int playerY;
        public int enemyX;
        public int enemyY;
        public boolean gameStarted;
        public boolean gameFinished;
    }
}
