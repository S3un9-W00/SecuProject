package com.example.secuproject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Maze_two {
    private int[][] map;
    private int size;
    private int playerX;
    private int playerY;
    private boolean itemEffect;
    private long endTime;
    private int viewRange = 1;

    public Maze_two() {
        size = 5;
        map = new int[][]{
                {0, 3, 4, 3, 9},
                {4, 3, 4, 3, 4},
                {3, 3, 3, 3, 4},
                {3, 4, 4, 6, 3},
                {3, 3, 3, 4, 3}
        };
        findStartSpot();
    }
    
    /**
     * MazeGenerator로 생성된 미로를 사용하여 Maze_two를 초기화합니다
     */
    public Maze_two(int[][] generatedMap) {
        if (generatedMap == null || generatedMap.length == 0) {
            // 기본 미로 사용
            size = 5;
            map = new int[][]{
                    {0, 3, 4, 3, 9},
                    {4, 3, 4, 3, 4},
                    {3, 3, 3, 3, 4},
                    {3, 4, 4, 6, 3},
                    {3, 3, 3, 4, 3}
            };
        } else {
            size = generatedMap.length;
            map = new int[size][size];
            for (int i = 0; i < size; i++) {
                System.arraycopy(generatedMap[i], 0, map[i], 0, size);
            }
        }
        findStartSpot();
    }
    
    /**
     * 텍스트 파일에서 미로를 읽어옵니다
     */
    public static Maze_two fromFile(String filePath) throws IOException {
        List<String> lines = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line.trim());
            }
        }
        
        if (lines.isEmpty()) {
            throw new IOException("파일이 비어있습니다");
        }
        
        int size = lines.size();
        int[][] map = new int[size][size];
        
        // 각 줄을 읽어서 미로를 구성합니다
        for (int i = 0; i < size; i++) {
            String line = lines.get(i);
            String[] parts = line.split("\\s+");
            
            for (int j = 0; j < parts.length && j < size; j++) {
                try {
                    map[i][j] = Integer.parseInt(parts[j]);
                } catch (NumberFormatException e) {
                    map[i][j] = 4; // 숫자가 아니면 벽으로 처리
                }
            }
        }
        
        return new Maze_two(map);
    }
    
    /**
     * Maze 모델을 Maze_two로 변환합니다
     */
    public static Maze_two fromMaze(com.example.secuproject.model.Maze maze) {
        int size = maze.getSize();
        int[][] map = new int[size][size];
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                com.example.secuproject.model.MazeCell cell = maze.getCell(i, j);
                map[i][j] = cell.getCode();
            }
        }
        
        return new Maze_two(map);
    }

    private void findStartSpot() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (map[i][j] == 0) {
                    playerX = i;
                    playerY = j;
                }
            }
        }
    }

    public String showMaze() {
        return showMaze(-1, -1); // Enemy 위치 없이 표시
    }
    
    /**
     * Enemy 위치를 포함하여 미로를 표시합니다
     * @param enemyX Enemy X 좌표 (-1이면 표시 안 함)
     * @param enemyY Enemy Y 좌표 (-1이면 표시 안 함)
     */
    public String showMaze(int enemyX, int enemyY) {
        long now = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();

        if (itemEffect && now > endTime) {
            sb.append("5초가 지나서 아이템 효과가 꺼집니다.\n");
            itemEffect = false;
            viewRange = 1;
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {

                boolean inView = Math.abs(i - playerX) <= viewRange &&
                        Math.abs(j - playerY) <= viewRange;

                if (!inView) {
                    sb.append(" ?");
                    continue;
                }

                // 플레이어와 Enemy 위치 우선 표시
                if (i == playerX && j == playerY) {
                    sb.append(" P"); // 플레이어
                } else if (enemyX >= 0 && enemyY >= 0 && i == enemyX && j == enemyY) {
                    sb.append(" E"); // Enemy
                } else if (map[i][j] == 4) {
                    sb.append(" #"); // 벽
                } else if (map[i][j] == 9) {
                    sb.append(" G"); // 도착지점
                } else if (map[i][j] == 6) {
                    sb.append(" *"); // 아이템
                } else {
                    sb.append(" ."); // 길
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public MoveResult move(char c) {
        int nx = playerX;
        int ny = playerY;

        if (c == 'w') nx--;
        else if (c == 'a') ny--;
        else if (c == 's') nx++;
        else if (c == 'd') ny++;

        if (nx < 0 || size <= nx || ny < 0 || size <= ny) {
            return new MoveResult(false, false, "범위를 벗어남");
        }

        if (map[nx][ny] == 4) {
            return new MoveResult(false, false, "벽입니다! 진입 불가");
        }

        if (map[nx][ny] == 9) {
            playerX = nx;
            playerY = ny;
            return new MoveResult(true, true, "도착입니다!");
        }

        playerX = nx;
        playerY = ny;

        String msg = "이동했습니다.";
        if (map[nx][ny] == 6) {
            msg = "아이템 효과 발동!";
            itemEffect = true;
            endTime = System.currentTimeMillis() + 5000;
            viewRange = 2;
            map[nx][ny] = 1;
        }

        return new MoveResult(true, false, msg);
    }

    public static class MoveResult {
        public boolean moved;
        public boolean arrived;
        public String message;

        public MoveResult(boolean moved, boolean arrived, String message) {
            this.moved = moved;
            this.arrived = arrived;
            this.message = message;
        }
    }
    public int[][] getMap() {
        return map;
    }

    public int getSize() {
        return size;
    }

    public int getPlayerX() {
        return playerX;
    }

    public int getPlayerY() {
        return playerY;
    }
}
