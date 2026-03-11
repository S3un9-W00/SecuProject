package com.example.secuproject.Service;

import com.example.secuproject.Maze_two;
import com.example.secuproject.Enemy;
import com.example.secuproject.log.GameLogger;
import com.example.secuproject.log.GameLog;
import com.example.secuproject.replay.ReplayFrame;
import com.example.secuproject.replay.ReplayGenerator;
import com.example.secuproject.util.MazeGenerator;
import com.example.secuproject.util.MazeValidator;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class MazeService {
    private Maze_two maze;
    private Enemy enemy;
    private boolean gameStarted = false;
    private boolean gameFinished = false;
    private boolean playerArrived = false;
    private boolean enemyArrived = false;
    private boolean logSaved = false;
    private MazeGenerator generator;
    private MazeValidator validator;
    private final SecureRandom random = new SecureRandom();
    private GameLogger gameLogger;
    private String lastSavedLogFile;

    public MazeService() {
        this.generator = new MazeGenerator();
        this.validator = new MazeValidator();
        this.gameLogger = new GameLogger();
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
        playerArrived = false;
        enemyArrived = false;
        logSaved = false;

        // 게임 로그 시작
        gameLogger.startNewGame(maze.getSize(), maze.getMap(),
            maze.getPlayerX(), maze.getPlayerY(), enemyStartX, enemyStartY);
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
        
        if (playerArrived) {
            return new Maze_two.MoveResult(false, false, "플레이어는 이미 도착했습니다. AI의 도착을 기다려주세요.");
        }
        
        Maze_two.MoveResult result = maze.move(dir);
        
        // 이동 로그 기록
        gameLogger.logEvent(
            "MOVE",
            dir,
            maze.getPlayerX(),
            maze.getPlayerY(),
            enemy != null ? enemy.getX() : -1,
            enemy != null ? enemy.getY() : -1,
            result.message,
            result.moved
        );
        
        // 도착했는지 확인
        if (result.arrived && !playerArrived) {
            playerArrived = true;
            // 한 명만 도착: 게임 계속 (gameFinished는 false 유지)
            // 두 명 다 도착하면 gameFinished = true
            if (playerArrived && enemyArrived) {
                gameFinished = true;
                logSaved = true;
                String logFile = gameLogger.finishAndSave(playerArrived, enemyArrived);
                if (logFile != null) {
                    lastSavedLogFile = logFile;
                    System.out.println("게임 완료 - 로그 저장됨: " + logFile);
                }
            }
        }
        
        return result;
    }

    /**
     * AI(Enemy) 이동 - 오른손 법칙으로 자동 이동
     */
    public void aiMove() {
        if (enemy != null && !gameFinished) {
            enemy.step();
            
            // Enemy 이동 로그 기록
            gameLogger.logEvent(
                "AI_MOVE",
                ' ',
                maze.getPlayerX(),
                maze.getPlayerY(),
                enemy.getX(),
                enemy.getY(),
                "AI moved",
                true
            );
            
            // Enemy가 도착지점에 도달했는지 확인
            int[][] map = maze.getMap();
            int ex = enemy.getX();
            int ey = enemy.getY();
            if (ex >= 0 && ex < map.length && ey >= 0 && ey < map[0].length) {
                if (map[ex][ey] == 9 && !enemyArrived) {
                    enemyArrived = true;
                    if (playerArrived && enemyArrived) {
                        gameFinished = true;
                        logSaved = true;
                        String logFile = gameLogger.finishAndSave(playerArrived, enemyArrived);
                        if (logFile != null) {
                            lastSavedLogFile = logFile;
                            System.out.println("게임 완료 - 로그 저장됨: " + logFile);
                        }
                    }
                }
            }
        }
    }

    /**
     * 게임 리셋 - 기존 로그 저장 후 새 게임 시작
     */
    public String reset() {
        String logFile = null;
        if (gameLogger.getCurrentLog() != null && !logSaved) {
            logFile = gameLogger.finishAndSave(playerArrived, enemyArrived);
            if (logFile != null) {
                lastSavedLogFile = logFile;
            }
        }
        startGame();
        return logFile;
    }

    /**
     * 게임 로그 저장 (게임 종료 시)
     */
    public String saveGameLog() {
        if (logSaved) {
            return lastSavedLogFile;
        }
        String logFile = gameLogger.finishAndSave(playerArrived, enemyArrived);
        if (logFile != null) {
            logSaved = true;
            lastSavedLogFile = logFile;
        }
        return logFile;
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
        status.playerArrived = playerArrived;
        status.enemyArrived = enemyArrived;
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
     * 게임 로거 반환
     */
    public GameLogger getGameLogger() {
        return gameLogger;
    }

    /**
     * 최근 저장된 로그 파일 경로
     */
    public String getLastSavedLogFile() {
        return lastSavedLogFile;
    }

    /**
     * 저장된 로그 파일 목록 (최신순)
     */
    public List<String> listSavedLogFiles() {
        File dir = new File("game_logs");
        if (!dir.exists() || !dir.isDirectory()) {
            return List.of();
        }

        File[] files = dir.listFiles((d, name) -> name != null && name.endsWith(".log"));
        if (files == null || files.length == 0) {
            return List.of();
        }

        List<File> fileList = new ArrayList<>();
        Collections.addAll(fileList, files);
        fileList.sort(Comparator.comparingLong(File::lastModified).reversed());

        List<String> paths = new ArrayList<>();
        for (File f : fileList) {
            paths.add(f.getPath());
        }
        return paths;
    }

    /**
     * 로그 파일을 로드하여 리플레이 프레임으로 변환합니다.
     */
    public List<ReplayFrame> loadReplayFrames(String logFilePath) {
        if (logFilePath == null || logFilePath.isBlank()) {
            return List.of();
        }
        GameLog log = gameLogger.loadLog(logFilePath);
        if (log == null) {
            return List.of();
        }
        return ReplayGenerator.generateFrames(log);
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
        public boolean playerArrived;
        public boolean enemyArrived;
    }
}
