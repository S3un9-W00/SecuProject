package com.example.secuproject.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class MazeGenerator {
    private Random random;
    
    private static final int[] DX = {-1, 1, 0, 0};
    private static final int[] DY = {0, 0, -1, 1};
    private static final int MAX_GENERATION_ATTEMPTS = 50;
    
    public MazeGenerator() {
        this.random = new Random();
    }
    
    public int[][] generateMaze(int size) {
        if (size < 5) {
            size = 5;
        }
        
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            int[][] map = initializeAllWalls(size);
            
            int[][] starts = placeStartPoints(map, size);
            
            generateMazeWithPrims(map, starts, size);
            
            int[] goal = placeGoalAtDepth(map, starts, size);
            if (goal == null) {
                continue;
            }
            
            if (validateMinimumPathLength(map, starts, goal, size)) {
                placeItems(map, size);
                return map;
            }
        }
        
        return generateFallbackMaze(size);
    }
    
    private int[][] initializeAllWalls(int size) {
        int[][] map = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                map[i][j] = 4;
            }
        }
        return map;
    }
    
    private int[][] placeStartPoints(int[][] map, int size) {
        int[][] starts = new int[2][2];
        
        // 랜덤 위치에 스타트 배치
        List<int[]> candidates = new ArrayList<>();
        
        for (int i = 1; i < size - 1; i++) {
            candidates.add(new int[]{i, 1});
        }
        
        for (int i = 1; i < size - 1; i++) {
            candidates.add(new int[]{1, i});
        }
        
        Collections.shuffle(candidates, random);
        
        int[] start1 = candidates.get(0);
        map[start1[0]][start1[1]] = 0;
        starts[0][0] = start1[0];
        starts[0][1] = start1[1];
        
        List<int[]> candidates2 = new ArrayList<>();
        
        for (int i = 1; i < size - 1; i++) {
            candidates2.add(new int[]{i, size - 2});
        }
        
        for (int i = 1; i < size - 1; i++) {
            candidates2.add(new int[]{size - 2, i});
        }
        
        Collections.shuffle(candidates2, random);
        
        int[] start2 = candidates2.get(0);
        map[start2[0]][start2[1]] = 0;
        starts[1][0] = start2[0];
        starts[1][1] = start2[1];
        
        return starts;
    }
    
    private void generateMazeWithPrims(int[][] map, int[][] starts, int size) {
        Set<String> visited = new HashSet<>();
        
        visited.add(starts[0][0] + "," + starts[0][1]);
        visited.add(starts[1][0] + "," + starts[1][1]);
        
        carveMazeDFS(map, starts[0][0], starts[0][1], size, visited, -1);
        
        String key2 = starts[1][0] + "," + starts[1][1];
        if (!visited.contains(key2) || calculateShortestPath(map, starts[0][0], starts[0][1], starts[1][0], starts[1][1]) < 0) {
            carvePathBetweenStarts(map, starts, visited, size);
        }
    }
    
    private void carveMazeDFS(int[][] map, int x, int y, int size, Set<String> visited, int lastDir) {
        Stack<int[]> stack = new Stack<>();
        stack.push(new int[]{x, y, lastDir});
        
        while (!stack.isEmpty()) {
            int[] current = stack.peek();
            int cx = current[0], cy = current[1], prevDir = current[2];
            
            List<int[]> neighbors = new ArrayList<>();
            for (int dir = 0; dir < 4; dir++) {
                int nx = cx + DX[dir] * 2;
                int ny = cy + DY[dir] * 2;
                
                if (nx > 0 && nx < size - 1 && ny > 0 && ny < size - 1) {
                    String key = nx + "," + ny;
                    if (!visited.contains(key)) {
                        neighbors.add(new int[]{nx, ny, dir});
                    }
                }
            }
            
            if (neighbors.isEmpty()) {
                stack.pop();
                continue;
            }
            
        int[] chosen = null;
        if (prevDir >= 0 && random.nextDouble() < 0.3) {
                for (int[] n : neighbors) {
                    if (n[2] == prevDir) {
                        chosen = n;
                        break;
                    }
                }
            }
            if (chosen == null) {
                chosen = neighbors.get(random.nextInt(neighbors.size()));
            }
            
            int nx = chosen[0], ny = chosen[1], dir = chosen[2];
            
            int wx = cx + DX[dir];
            int wy = cy + DY[dir];
            
            map[wx][wy] = 3;
            map[nx][ny] = 3;
            
            visited.add(wx + "," + wy);
            visited.add(nx + "," + ny);
            
            stack.push(new int[]{nx, ny, dir});
        }
    }
    
    private void carvePathBetweenStarts(int[][] map, int[][] starts, Set<String> visited, int size) {
        int start1X = starts[0][0], start1Y = starts[0][1];
        int start2X = starts[1][0], start2Y = starts[1][1];
        
        Queue<int[]> queue = new LinkedList<>();
        Map<String, int[]> parent = new HashMap<>();
        
        String key2 = start2X + "," + start2Y;
        queue.add(new int[]{start2X, start2Y});
        parent.put(key2, null);
        
        int[] target = null;
        while (!queue.isEmpty() && target == null) {
            int[] curr = queue.poll();
            
            for (int i = 0; i < 4; i++) {
                int nx = curr[0] + DX[i];
                int ny = curr[1] + DY[i];
                
                if (nx <= 0 || nx >= size - 1 || ny <= 0 || ny >= size - 1) continue;
                
                String nkey = nx + "," + ny;
                if (parent.containsKey(nkey)) continue;
                
                parent.put(nkey, curr);
                
                if (map[nx][ny] == 3 || map[nx][ny] == 0) {
                    target = new int[]{nx, ny};
                    break;
                }
                
                queue.add(new int[]{nx, ny});
            }
        }
        
        if (target != null) {
            int[] curr = target;
            while (curr != null) {
                String key = curr[0] + "," + curr[1];
                if (map[curr[0]][curr[1]] == 4) {
                    map[curr[0]][curr[1]] = 3;
                    visited.add(key);
                }
                curr = parent.get(key);
            }
        }
    }
    
    private int[] placeGoalAtDepth(int[][] map, int[][] starts, int size) {
        int minDepth = size * 2;
        
        List<int[]> walkableCells = new ArrayList<>();
        for (int i = 1; i < size - 1; i++) {
            for (int j = 1; j < size - 1; j++) {
                if (map[i][j] == 3) {
                    walkableCells.add(new int[]{i, j});
                }
            }
        }
        
        List<int[]> candidates = new ArrayList<>();
        
        for (int[] cell : walkableCells) {
            boolean validForAllStarts = true;
            
            for (int[] start : starts) {
                int dist = calculateShortestPath(map, start[0], start[1], cell[0], cell[1]);
                if (dist < 0 || dist < minDepth) {
                    validForAllStarts = false;
                    break;
                }
            }
            
            if (validForAllStarts) {
                candidates.add(cell);
            }
        }
        
        if (candidates.isEmpty()) {
            return null;
        }
        
        int[] goal = candidates.get(random.nextInt(candidates.size()));
        map[goal[0]][goal[1]] = 9;
        return goal;
    }
    
    private int calculateShortestPath(int[][] map, int startX, int startY, int goalX, int goalY) {
        int size = map.length;
        Queue<int[]> queue = new LinkedList<>();
        Map<String, Integer> distance = new HashMap<>();
        
        String startKey = startX + "," + startY;
        queue.add(new int[]{startX, startY});
        distance.put(startKey, 0);
        
        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            String currentKey = current[0] + "," + current[1];
            int currentDist = distance.get(currentKey);
            
            if (current[0] == goalX && current[1] == goalY) {
                return currentDist;
            }
            
            for (int i = 0; i < 4; i++) {
                int nx = current[0] + DX[i];
                int ny = current[1] + DY[i];
                String key = nx + "," + ny;
                
                if (nx >= 0 && nx < size && ny >= 0 && ny < size && !distance.containsKey(key)) {
                    if (map[nx][ny] == 3 || map[nx][ny] == 9 || map[nx][ny] == 0) {
                        distance.put(key, currentDist + 1);
                        queue.add(new int[]{nx, ny});
                    }
                }
            }
        }
        
        return -1;
    }
    
    private boolean validateMinimumPathLength(int[][] map, int[][] starts, int[] goal, int size) {
        int minimumRequired = size * 2;
        
        if (goal == null) {
            return false;
        }
        
        for (int[] start : starts) {
            int pathLength = calculateShortestPath(map, start[0], start[1], goal[0], goal[1]);
            if (pathLength < 0 || pathLength < minimumRequired) {
                return false;
            }
        }
        
        return true;
    }
    
    private void placeItems(int[][] map, int size) {
        int itemCount = size / 3;
        
        for (int i = 0; i < itemCount; i++) {
            int attempts = 0;
            while (attempts < 50) {
                int x = 1 + random.nextInt(size - 2);
                int y = 1 + random.nextInt(size - 2);
                
                if (map[x][y] == 3) {
                    map[x][y] = 6;
                    break;
                }
                attempts++;
            }
        }
    }
    
    private int[][] generateFallbackMaze(int size) {
        int[][] map = initializeAllWalls(size);
        int[][] starts = placeStartPoints(map, size);
        generateMazeWithPrims(map, starts, size);
        
        List<int[]> walkableCells = new ArrayList<>();
        for (int i = 1; i < size - 1; i++) {
            for (int j = 1; j < size - 1; j++) {
                if (map[i][j] == 3) {
                    walkableCells.add(new int[]{i, j});
                }
            }
        }
        
        int[] bestGoal = null;
        int bestMinDist = -1;
        
        for (int[] cell : walkableCells) {
            int minDistFromStarts = Integer.MAX_VALUE;
            boolean reachable = true;
            
            for (int[] start : starts) {
                int dist = calculateShortestPath(map, start[0], start[1], cell[0], cell[1]);
                if (dist < 0) {
                    reachable = false;
                    break;
                }
                minDistFromStarts = Math.min(minDistFromStarts, dist);
            }
            
            if (reachable && minDistFromStarts > bestMinDist) {
                bestMinDist = minDistFromStarts;
                bestGoal = cell;
            }
        }
        
        if (bestGoal != null) {
            map[bestGoal[0]][bestGoal[1]] = 9;
        } else if (!walkableCells.isEmpty()) {
            int[] goal = walkableCells.get(random.nextInt(walkableCells.size()));
            map[goal[0]][goal[1]] = 9;
        }
        
        placeItems(map, size);
        
        return map;
    }
    
    public void saveToFile(int[][] map, String filePath) throws IOException {
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            int size = map.length;
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    writer.print(map[i][j]);
                    if (j < size - 1) {
                        writer.print(" ");
                    }
                }
                writer.println();
            }
        }
    }
    
    public static void main(String[] args) {
        MazeGenerator generator = new MazeGenerator();
        MazeValidator validator = new MazeValidator();
        
        int size = 10;
        if (args.length > 0) {
            try {
                size = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("잘못된 크기입니다. 기본 크기 10을 사용합니다.");
            }
        }
        
        String filePath = "maze.txt";
        if (args.length > 1) {
            filePath = args[1];
        }
        
        System.out.println("미로 생성 중... (크기: " + size + "x" + size + ")");
        
        int attempts = 0;
        int[][] map = null;
        while (attempts < 10) {
            map = generator.generateMaze(size);
            MazeValidator.ValidationResult result = validator.validate(map);
            if (result.valid) {
                System.out.println("미로 검증 성공!");
                break;
            }
            System.out.println("미로 검증 실패, 다시 생성 중... (" + (attempts + 1) + "/10)");
            attempts++;
        }
        
        if (map == null || attempts >= 10) {
            System.out.println("유효한 미로를 생성하지 못했습니다.");
            return;
        }
        
        try {
            generator.saveToFile(map, filePath);
            System.out.println("미로가 저장되었습니다: " + filePath);
        } catch (IOException e) {
            System.out.println("파일 저장 중 오류 발생: " + e.getMessage());
        }
    }
}
