package com.example.secuproject.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 미로 맵을 나타내는 클래스
 * N × N 크기의 2차원 배열로 미로를 저장합니다
 */
public class Maze {
    private int size; // 미로 크기 (N)
    private MazeCell[][] cells; // 미로 셀 배열
    private List<Position> startPositions; // 스타트 지점들 (0)
    private Position goalPosition; // 도착지점 (9)

    public Maze(int size) {
        this.size = size;
        this.cells = new MazeCell[size][size];
        this.startPositions = new ArrayList<>();
        
        // 모든 칸을 벽(4)으로 초기화
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                cells[i][j] = new MazeCell(4);
            }
        }
    }

    public int getSize() {
        return size;
    }

    public MazeCell getCell(int x, int y) {
        if (isValidPosition(x, y)) {
            return cells[x][y];
        }
        return null;
    }

    public MazeCell getCell(Position pos) {
        return getCell(pos.getX(), pos.getY());
    }

    public void setCell(int x, int y, int code) {
        if (isValidPosition(x, y)) {
            cells[x][y].setCode(code);
            
            // 스타트 지점이나 도착지점 추적
            if (code == 0) {
                Position start = new Position(x, y);
                if (!startPositions.contains(start)) {
                    startPositions.add(start);
                }
            } else if (code == 9) {
                goalPosition = new Position(x, y);
            }
        }
    }

    public void setCell(Position pos, int code) {
        setCell(pos.getX(), pos.getY(), code);
    }

    /**
     * 위치가 미로 범위 내에 있는지 확인합니다
     */
    public boolean isValidPosition(int x, int y) {
        return x >= 0 && x < size && y >= 0 && y < size;
    }

    public boolean isValidPosition(Position pos) {
        return isValidPosition(pos.getX(), pos.getY());
    }

    /**
     * 특정 위치가 이동 가능한지 확인합니다
     */
    public boolean isWalkable(int x, int y) {
        if (!isValidPosition(x, y)) {
            return false;
        }
        return getCell(x, y).isWalkable();
    }

    public boolean isWalkable(Position pos) {
        return isWalkable(pos.getX(), pos.getY());
    }

    public List<Position> getStartPositions() {
        return new ArrayList<>(startPositions);
    }

    public Position getGoalPosition() {
        return goalPosition;
    }

    /**
     * 안개 시스템: 플레이어 주변 3×3 범위(양옆앞뒤대각선 1칸씩)를 가시화합니다
     * 아이템이 활성화되어 있으면 5×5 범위를 가시화합니다
     */
    public void updateVisibility(Position playerPos, boolean hasItem) {
        // 먼저 모든 칸을 안개로 가립니다
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                cells[i][j].setVisible(false);
            }
        }

        // 기본: 플레이어 기준 양옆앞뒤대각선으로 1칸씩 (3×3 범위, range = 1)
        // 아이템 있을 때: 5×5 범위 (range = 2)
        int range = hasItem ? 2 : 1;
        
        // 플레이어 주변 범위만 가시화
        for (int dx = -range; dx <= range; dx++) {
            for (int dy = -range; dy <= range; dy++) {
                int x = playerPos.getX() + dx;
                int y = playerPos.getY() + dy;
                if (isValidPosition(x, y)) {
                    cells[x][y].setVisible(true);
                }
            }
        }
    }
    
    /**
     * 여러 플레이어의 가시성을 합칩니다 (양쪽 플레이어 모두의 시야를 표시)
     */
    public void updateVisibilityForAll(List<Position> playerPositions, List<Boolean> hasItems) {
        // 먼저 모든 칸을 안개로 가립니다
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                cells[i][j].setVisible(false);
            }
        }
        
        // 각 플레이어의 시야를 합칩니다
        for (int idx = 0; idx < playerPositions.size(); idx++) {
            Position playerPos = playerPositions.get(idx);
            boolean hasItem = idx < hasItems.size() ? hasItems.get(idx) : false;
            // 기본: 플레이어 기준 양옆앞뒤대각선으로 1칸씩 (3×3 범위, range = 1)
            // 아이템 있을 때: 5×5 범위 (range = 2)
            int range = hasItem ? 2 : 1;
            
            for (int dx = -range; dx <= range; dx++) {
                for (int dy = -range; dy <= range; dy++) {
                    int x = playerPos.getX() + dx;
                    int y = playerPos.getY() + dy;
                    if (isValidPosition(x, y)) {
                        cells[x][y].setVisible(true);
                    }
                }
            }
        }
    }
}

