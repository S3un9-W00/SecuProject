package com.example.secuproject.log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 전체 게임 플레이 로그를 관리합니다
 * JSON/직렬화 형태로 저장 가능합니다
 */
public class GameLog implements Serializable {
    private static final long serialVersionUID = 1L;

    private String gameId;              // 게임 고유 ID (UUID 권장)
    private long startTime;             // 게임 시작 시간
    private long endTime;               // 게임 종료 시간
    private int mazeSize;               // 미로 크기
    private int[][] initialMaze;        // 게임 시작 시 미로 상태
    private int initialPlayerX;         // 초기 플레이어 X
    private int initialPlayerY;         // 초기 플레이어 Y
    private int initialEnemyX;          // 초기 AI X
    private int initialEnemyY;          // 초기 AI Y
    private List<GameEvent> events;     // 게임 중 발생한 이벤트들
    private boolean playerWon;          // 플레이어 승리 여부
    private boolean enemyWon;           // AI 승리 여부

    public GameLog() {
        this.events = new ArrayList<>();
        this.gameId = java.util.UUID.randomUUID().toString();
        this.startTime = System.currentTimeMillis();
    }

    public void addEvent(GameEvent event) {
        events.add(event);
    }

    public void finishGame(boolean playerWon, boolean enemyWon) {
        this.endTime = System.currentTimeMillis();
        this.playerWon = playerWon;
        this.enemyWon = enemyWon;
    }

    // Getters & Setters
    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }

    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }

    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }

    public int getMazeSize() { return mazeSize; }
    public void setMazeSize(int mazeSize) { this.mazeSize = mazeSize; }

    public int[][] getInitialMaze() { return initialMaze; }
    public void setInitialMaze(int[][] initialMaze) { this.initialMaze = initialMaze; }

    public int getInitialPlayerX() { return initialPlayerX; }
    public void setInitialPlayerX(int initialPlayerX) { this.initialPlayerX = initialPlayerX; }

    public int getInitialPlayerY() { return initialPlayerY; }
    public void setInitialPlayerY(int initialPlayerY) { this.initialPlayerY = initialPlayerY; }

    public int getInitialEnemyX() { return initialEnemyX; }
    public void setInitialEnemyX(int initialEnemyX) { this.initialEnemyX = initialEnemyX; }

    public int getInitialEnemyY() { return initialEnemyY; }
    public void setInitialEnemyY(int initialEnemyY) { this.initialEnemyY = initialEnemyY; }

    public List<GameEvent> getEvents() { return events; }
    public void setEvents(List<GameEvent> events) { this.events = events; }

    public boolean isPlayerWon() { return playerWon; }
    public void setPlayerWon(boolean playerWon) { this.playerWon = playerWon; }

    public boolean isEnemyWon() { return enemyWon; }
    public void setEnemyWon(boolean enemyWon) { this.enemyWon = enemyWon; }

    public long getDurationMillis() {
        return endTime - startTime;
    }

    public String getDurationString() {
        long duration = getDurationMillis();
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d분 %d초", minutes, seconds);
    }

    @Override
    public String toString() {
        return String.format("GameLog{gameId='%s', duration=%s, events=%d, player=%s, enemy=%s}",
            gameId, getDurationString(), events.size(), 
            playerWon ? "WIN" : "LOSE", enemyWon ? "WIN" : "LOSE");
    }
}
