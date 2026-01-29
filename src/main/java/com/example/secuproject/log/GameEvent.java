package com.example.secuproject.log;

import java.io.Serializable;

public class GameEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    private long timestamp;           // 이벤트 발생 시간 (밀리초)
    private String eventType;         // MOVE, ITEM_TORCH, ITEM_HAMMER, TRAP, ARRIVE, AI_MOVE 등
    private char direction;           // 이동 방향 (w/a/s/d, AI_MOVE는 공백)
    private int playerX;              // 플레이어 X 좌표
    private int playerY;              // 플레이어 Y 좌표
    private int enemyX;               // AI X 좌표
    private int enemyY;               // AI Y 좌표
    private String message;           // 이벤트 결과 메시지
    private boolean success;          // 성공 여부

    public GameEvent() {}

    public GameEvent(long timestamp, String eventType, char direction, 
                     int playerX, int playerY, int enemyX, int enemyY,
                     String message, boolean success) {
        this.timestamp = timestamp;
        this.eventType = eventType;
        this.direction = direction;
        this.playerX = playerX;
        this.playerY = playerY;
        this.enemyX = enemyX;
        this.enemyY = enemyY;
        this.message = message;
        this.success = success;
    }

    // Getters & Setters
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public char getDirection() { return direction; }
    public void setDirection(char direction) { this.direction = direction; }

    public int getPlayerX() { return playerX; }
    public void setPlayerX(int playerX) { this.playerX = playerX; }

    public int getPlayerY() { return playerY; }
    public void setPlayerY(int playerY) { this.playerY = playerY; }

    public int getEnemyX() { return enemyX; }
    public void setEnemyX(int enemyX) { this.enemyX = enemyX; }

    public int getEnemyY() { return enemyY; }
    public void setEnemyY(int enemyY) { this.enemyY = enemyY; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    @Override
    public String toString() {
        return String.format("[%d] %s(%c) P(%d,%d) E(%d,%d) | %s", 
            timestamp, eventType, direction, playerX, playerY, enemyX, enemyY, message);
    }
}
