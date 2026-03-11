package com.example.secuproject.util;

import java.util.*;

public class MazeValidator {
    
    public ValidationResult validate(int[][] map) {
        if (map == null || map.length == 0) {
            return new ValidationResult(false, "미로가 비어있습니다.");
        }
        
        int size = map.length;
        
        List<int[]> starts = findStarts(map, size);
        if (starts.size() < 1) {
            return new ValidationResult(false, "스타트 지점(0)이 없습니다.");
        }
        
        int[] goal = findGoal(map, size);
        if (goal == null) {
            return new ValidationResult(false, "도착지점(9)이 없습니다.");
        }
        
        for (int[] start : starts) {
            if (!hasPath(map, start[0], start[1], goal[0], goal[1], size)) {
                return new ValidationResult(false, 
                    String.format("스타트 지점 (%d, %d)에서 도착지점까지 경로가 없습니다.", 
                        start[0], start[1]));
            }
        }
        
        if (starts.size() >= 2) {
            int pathCount = 0;
            for (int[] start : starts) {
                if (hasPath(map, start[0], start[1], goal[0], goal[1], size)) {
                    pathCount++;
                }
            }
            if (pathCount < 2) {
                return new ValidationResult(false, "최소 2개 이상의 경로가 필요합니다.");
            }
        }
        
        if (!areAllWallsConnected(map, size)) {
            return new ValidationResult(false, "벽(4)이 여러 개의 분리된 영역으로 나뉘어 있습니다. 모든 벽은 하나로 연결되어야 합니다.");
        }
        
        int minPathRequired = size * 2;
        for (int[] start : starts) {
            int shortestPath = getShortestPathLength(map, start[0], start[1], goal[0], goal[1], size);
            if (shortestPath < minPathRequired) {
                return new ValidationResult(false, 
                    String.format("스타트 지점 (%d, %d)에서 도착지점까지의 최단 경로가 %d칸으로, 최소 필요 거리(%d칸)보다 짧습니다.", 
                        start[0], start[1], shortestPath, minPathRequired));
            }
        }
        
        return new ValidationResult(true, "미로가 유효합니다.");
    }
    
    private List<int[]> findStarts(int[][] map, int size) {
        List<int[]> starts = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (map[i][j] == 0) {
                    starts.add(new int[]{i, j});
                }
            }
        }
        return starts;
    }
    
    private int[] findGoal(int[][] map, int size) {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (map[i][j] == 9) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }
    
    private boolean hasPath(int[][] map, int startX, int startY, int goalX, int goalY, int size) {
        Queue<int[]> queue = new LinkedList<>();
        boolean[][] visited = new boolean[size][size];
        
        queue.add(new int[]{startX, startY});
        visited[startX][startY] = true;
        
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        
        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int x = current[0];
            int y = current[1];
            
            if (x == goalX && y == goalY) {
                return true;
            }
            
            for (int i = 0; i < 4; i++) {
                int nx = x + dx[i];
                int ny = y + dy[i];
                
                if (nx >= 0 && nx < size && ny >= 0 && ny < size && !visited[nx][ny]) {
                    if (map[nx][ny] != 4) {
                        visited[nx][ny] = true;
                        queue.add(new int[]{nx, ny});
                    }
                }
            }
        }
        
        return false;
    }
    
    private boolean areAllWallsConnected(int[][] map, int size) {
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        
        boolean[][] visited = new boolean[size][size];
        
        int firstWallX = -1, firstWallY = -1;
        outer:
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (map[i][j] == 4) {
                    firstWallX = i;
                    firstWallY = j;
                    break outer;
                }
            }
        }
        
        if (firstWallX == -1) return true;
        
        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{firstWallX, firstWallY});
        visited[firstWallX][firstWallY] = true;
        int wallsFound = 0;
        
        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            wallsFound++;
            
            for (int i = 0; i < 4; i++) {
                int nx = current[0] + dx[i];
                int ny = current[1] + dy[i];
                
                if (nx >= 0 && nx < size && ny >= 0 && ny < size 
                    && !visited[nx][ny] && map[nx][ny] == 4) {
                    visited[nx][ny] = true;
                    queue.add(new int[]{nx, ny});
                }
            }
        }
        
        int totalWalls = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (map[i][j] == 4) totalWalls++;
            }
        }
        
        return wallsFound == totalWalls;
    }
    
    private int getShortestPathLength(int[][] map, int startX, int startY, int goalX, int goalY, int size) {
        Queue<int[]> queue = new LinkedList<>();
        boolean[][] visited = new boolean[size][size];
        
        queue.add(new int[]{startX, startY, 0});
        visited[startX][startY] = true;
        
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        
        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int x = current[0];
            int y = current[1];
            int dist = current[2];
            
            if (x == goalX && y == goalY) {
                return dist;
            }
            
            for (int i = 0; i < 4; i++) {
                int nx = x + dx[i];
                int ny = y + dy[i];
                
                if (nx >= 0 && nx < size && ny >= 0 && ny < size && !visited[nx][ny]) {
                    if (map[nx][ny] != 4) {
                        visited[nx][ny] = true;
                        queue.add(new int[]{nx, ny, dist + 1});
                    }
                }
            }
        }
        
        return -1;
    }
    
    public static class ValidationResult {
        public boolean valid;
        public String message;
        
        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
    }
}
