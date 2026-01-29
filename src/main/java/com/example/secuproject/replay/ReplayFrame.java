package com.example.secuproject.replay;

public class ReplayFrame {
    public int index;
    public long offsetMillis;   // 시작 프레임 대비 경과 시간
    public String eventType;
    public char direction;
    public int playerX;
    public int playerY;
    public int enemyX;
    public int enemyY;
    public String message;
    public boolean success;
    public String mazeView;

    public ReplayFrame() {}
}

