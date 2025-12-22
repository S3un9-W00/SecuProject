package com.example.secuproject.model;

/**
 * 플레이어를 나타내는 클래스
 * '나'(플레이어 1)와 '너'(플레이어 2)를 구분합니다
 */
public class Player {
    private PlayerType type; // 나(ME) 또는 너(YOU)
    private Position position; // 현재 위치
    private boolean hasItem; // 아이템 보유 여부
    private long itemExpireTime; // 아이템 효과 만료 시간 (밀리초)
    private boolean reachedGoal; // 도착지점 도달 여부
    private long finishTime; // 완료 시간 (밀리초)

    public Player(PlayerType type, Position position) {
        this.type = type;
        this.position = position;
        this.hasItem = false;
        this.itemExpireTime = 0;
        this.reachedGoal = false;
        this.finishTime = 0;
    }

    public PlayerType getType() {
        return type;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public boolean hasItem() {
        return hasItem && System.currentTimeMillis() < itemExpireTime;
    }

    public void activateItem() {
        this.hasItem = true;
        this.itemExpireTime = System.currentTimeMillis() + 5000; // 5초
    }

    public void deactivateItem() {
        this.hasItem = false;
    }

    public boolean isReachedGoal() {
        return reachedGoal;
    }

    public void setReachedGoal(boolean reachedGoal) {
        this.reachedGoal = reachedGoal;
    }
    
    public void setFinishTime(long elapsedTime) {
        if (finishTime == 0) {
            this.finishTime = elapsedTime;
        }
    }

    public long getFinishTime() {
        return finishTime;
    }

    /**
     * 플레이어 타입 열거형
     */
    public enum PlayerType {
        ME(1),   // 나 (플레이어 1)
        YOU(2);  // 너 (플레이어 2)

        private final int code;

        PlayerType(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
}

