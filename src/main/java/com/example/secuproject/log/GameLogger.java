package com.example.secuproject.log;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GameLogger {
    private static final String LOG_DIR = "game_logs";
    private GameLog currentLog;

    public GameLogger() {
        // 로그 디렉토리 생성
        File logDir = new File(LOG_DIR);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
    }

    /**
     * 새 게임 로그 시작
     */
    public void startNewGame(int mazeSize, int[][] maze, 
                           int playerX, int playerY, int enemyX, int enemyY) {
        currentLog = new GameLog();
        currentLog.setMazeSize(mazeSize);
        currentLog.setInitialMaze(copyMaze(maze));
        currentLog.setInitialPlayerX(playerX);
        currentLog.setInitialPlayerY(playerY);
        currentLog.setInitialEnemyX(enemyX);
        currentLog.setInitialEnemyY(enemyY);
    }

    /**
     * 이벤트 기록
     */
    public void logEvent(String eventType, char direction, 
                        int playerX, int playerY, int enemyX, int enemyY,
                        String message, boolean success) {
        if (currentLog == null) {
            return;
        }
        GameEvent event = new GameEvent(
            System.currentTimeMillis(),
            eventType,
            direction,
            playerX,
            playerY,
            enemyX,
            enemyY,
            message,
            success
        );
        currentLog.addEvent(event);
    }

    /**
     * 게임 종료 및 로그 저장
     */
    public String finishAndSave(boolean playerWon, boolean enemyWon) {
        if (currentLog == null) {
            return null;
        }
        currentLog.finishGame(playerWon, enemyWon);
        return saveLog(currentLog);
    }

    /**
     * 로그를 파일로 저장 (직렬화)
     */
    private String saveLog(GameLog log) {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = String.format("%s/game_%s_%s.log", 
                LOG_DIR, timestamp, log.getGameId().substring(0, 8));

            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(filename))) {
                oos.writeObject(log);
                oos.flush();
                System.out.println("✓ 로그 저장: " + filename);
                return filename;
            }
        } catch (IOException e) {
            System.err.println("✗ 로그 저장 실패: " + e.getMessage());
            return null;
        }
    }

    /**
     * 파일에서 로그 로드 (역직렬화)
     */
    public GameLog loadLog(String filename) {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(filename))) {
            GameLog log = (GameLog) ois.readObject();
            System.out.println("✓ 로그 로드: " + filename);
            return log;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("✗ 로그 로드 실패: " + e.getMessage());
            return null;
        }
    }

    /**
     * 2D 배열 복사 (깊은 복사)
     */
    private int[][] copyMaze(int[][] maze) {
        if (maze == null) return null;
        int[][] copy = new int[maze.length][];
        for (int i = 0; i < maze.length; i++) {
            copy[i] = maze[i].clone();
        }
        return copy;
    }

    public GameLog getCurrentLog() {
        return currentLog;
    }

    public void setCurrentLog(GameLog log) {
        currentLog = log;
    }
}
