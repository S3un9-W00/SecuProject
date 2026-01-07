package com.example.secuproject.util;

import java.util.*;

/**
 * 미로 검증기
 * 생성된 미로가 유효한지 검증합니다
 */
public class MazeValidator {
    
    /**
     * 미로가 유효한지 검증합니다
     * @param map 미로 맵
     * @return 검증 결과
     */
    public ValidationResult validate(int[][] map) {
        if (map == null || map.length == 0) {
            return new ValidationResult(false, "미로가 비어있습니다.");
        }
        
        int size = map.length;
        
        // 1. 스타트 지점 확인
        List<int[]> starts = findStarts(map, size);
        if (starts.size() < 1) {
            return new ValidationResult(false, "스타트 지점(0)이 없습니다.");
        }
        
        // 2. 도착지점 확인
        int[] goal = findGoal(map, size);
        if (goal == null) {
            return new ValidationResult(false, "도착지점(9)이 없습니다.");
        }
        
        // 3. 각 스타트 지점에서 도착지점까지 경로 확인
        for (int[] start : starts) {
            if (!hasPath(map, start[0], start[1], goal[0], goal[1], size)) {
                return new ValidationResult(false, 
                    String.format("스타트 지점 (%d, %d)에서 도착지점까지 경로가 없습니다.", 
                        start[0], start[1]));
            }
        }
        
        // 4. 최소 2개 이상의 경로 확인 (스타트가 2개 이상인 경우)
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
        
        return new ValidationResult(true, "미로가 유효합니다.");
    }
    
    /**
     * 스타트 지점들을 찾습니다
     */
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
    
    /**
     * 도착지점을 찾습니다
     */
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
    
    /**
     * 두 지점 사이에 경로가 있는지 확인 (BFS)
     */
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
                    if (map[nx][ny] != 4) { // 벽이 아니면 이동 가능
                        visited[nx][ny] = true;
                        queue.add(new int[]{nx, ny});
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * 검증 결과 클래스
     */
    public static class ValidationResult {
        public boolean valid;
        public String message;
        
        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
    }
}

