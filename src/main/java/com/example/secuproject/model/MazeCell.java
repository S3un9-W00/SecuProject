package com.example.secuproject.model;

/**
 * 미로의 한 칸을 나타내는 클래스
 * 각 칸은 숫자 코드로 구분됩니다:
 * 0: 스타트 지점, 1: 나 플레이어, 2: 너 플레이어, 3: 길, 4: 벽, 5: 안개, 6: 아이템, 9: 도착지점
 */
public class MazeCell {
    private int code;
    private boolean visible; // 안개 시스템을 위한 가시성

    public MazeCell(int code) {
        this.code = code;
        this.visible = false; // 기본적으로 안개로 가려져 있음
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * 이동 가능한 칸인지 확인합니다
     * (길, 스타트, 도착지점, 아이템은 이동 가능)
     * 벽(4)과 안개(5)는 이동 불가
     * 기타 코드(7, 8 등)도 이동 가능하게 처리
     */
    public boolean isWalkable() {
        // 벽(4)과 안개(5)만 이동 불가
        return code != 4 && code != 5;
    }

    /**
     * 플레이어가 있는지 확인합니다
     */
    public boolean hasPlayer() {
        return code == 1 || code == 2;
    }

    /**
     * 아이템이 있는지 확인합니다
     */
    public boolean hasItem() {
        return code == 6;
    }
}

