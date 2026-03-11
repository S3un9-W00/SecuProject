package com.example.secuproject.generator;

import com.example.secuproject.model.Maze;
import com.example.secuproject.model.MazeCell;
import com.example.secuproject.model.Position;

import java.util.*;

public class MazeGenerator {
    private Random random;
    
    private static final int[] DX = {-1, 1, 0, 0};
    private static final int[] DY = {0, 0, -1, 1};
    private static final int MAX_GENERATION_ATTEMPTS = 50;
    
    public MazeGenerator() {
        this.random = new Random();
    }
    
    public Maze generateMaze(int size) {
        if (size < 5) {
            size = 5;
        }
        
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            Maze maze = new Maze(size);
            
            initializeAllWalls(maze, size);
            
            List<Position> starts = placeStartPoints(maze, size);
            
            generateMazeWithPrims(maze, starts, size);
            
            Position goal = placeGoalAtDepth(maze, starts, size);
            if (goal == null) {
                continue;
            }
            
            if (validateMinimumPathLength(maze, size)) {
                placeItems(maze, size);
                initializeFog(maze, size);
                return maze;
            }
        }
        
        return generateFallbackMaze(size);
    }
    
    private void initializeAllWalls(Maze maze, int size) {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                maze.setCell(i, j, 4);
            }
        }
    }
    
    private List<Position> placeStartPoints(Maze maze, int size) {
        List<Position> starts = new ArrayList<>();
        
        int midY = size / 2;
        
        int x1 = 1;
        maze.setCell(x1, midY, 0);
        starts.add(new Position(x1, midY));
        
        int x2 = size - 2;
        maze.setCell(x2, midY, 0);
        starts.add(new Position(x2, midY));
        
        return starts;
    }
    
    private void generateMazeWithPrims(Maze maze, List<Position> starts, int size) {
        Set<String> visited = new HashSet<>();
        
        for (Position start : starts) {
            visited.add(start.getX() + "," + start.getY());
        }
        
        carveMazeDFS(maze, starts.get(0), size, visited, -1);
        
        if (starts.size() > 1) {
            Position start2 = starts.get(1);
            String key2 = start2.getX() + "," + start2.getY();
            
            if (!visited.contains(key2) || calculateShortestPath(maze, starts.get(0), start2) < 0) {
                carvePathBetweenStarts(maze, starts, visited, size);
            }
        }
    }
    
    private void carveMazeDFS(Maze maze, Position start, int size, Set<String> visited, int lastDir) {
        Stack<int[]> stack = new Stack<>();
        stack.push(new int[]{start.getX(), start.getY(), lastDir});
        
        while (!stack.isEmpty()) {
            int[] current = stack.peek();
            int x = current[0], y = current[1], prevDir = current[2];
            
            List<int[]> neighbors = new ArrayList<>();
            for (int dir = 0; dir < 4; dir++) {
                int nx = x + DX[dir] * 2;
                int ny = y + DY[dir] * 2;
                
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
            if (prevDir >= 0 && random.nextDouble() < 0.7) {
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
            
            int wx = x + DX[dir];
            int wy = y + DY[dir];
            
            maze.setCell(wx, wy, 3);
            maze.setCell(nx, ny, 3);
            
            visited.add(wx + "," + wy);
            visited.add(nx + "," + ny);
            
            stack.push(new int[]{nx, ny, dir});
        }
    }
    
    private void carvePathBetweenStarts(Maze maze, List<Position> starts, Set<String> visited, int size) {
        Position start1 = starts.get(0);
        Position start2 = starts.get(1);
        
        Queue<Position> queue = new LinkedList<>();
        Map<String, Position> parent = new HashMap<>();
        
        String key2 = start2.getX() + "," + start2.getY();
        queue.add(start2);
        parent.put(key2, null);
        
        Position target = null;
        while (!queue.isEmpty() && target == null) {
            Position curr = queue.poll();
            
            for (int i = 0; i < 4; i++) {
                int nx = curr.getX() + DX[i];
                int ny = curr.getY() + DY[i];
                
                if (nx <= 0 || nx >= size - 1 || ny <= 0 || ny >= size - 1) continue;
                
                String nkey = nx + "," + ny;
                if (parent.containsKey(nkey)) continue;
                
                parent.put(nkey, curr);
                
                MazeCell cell = maze.getCell(nx, ny);
                if (cell.getCode() == 3 || cell.getCode() == 0) {
                    target = new Position(nx, ny);
                    break;
                }
                
                queue.add(new Position(nx, ny));
            }
        }
        
        if (target != null) {
            Position curr = target;
            while (curr != null) {
                String key = curr.getX() + "," + curr.getY();
                MazeCell cell = maze.getCell(curr.getX(), curr.getY());
                if (cell.getCode() == 4) {
                    maze.setCell(curr.getX(), curr.getY(), 3);
                    visited.add(key);
                }
                curr = parent.get(key);
            }
        }
    }
    
    private void addNeighborWallsToFrontier(Maze maze, Position cell, 
            Set<String> passages, Set<String> frontier, int size) {
        
        for (int i = 0; i < 4; i++) {
            int nx = cell.getX() + DX[i];
            int ny = cell.getY() + DY[i];
            String key = nx + "," + ny;
            
            if (nx > 0 && nx < size - 1 && ny > 0 && ny < size - 1) {
                if (!passages.contains(key)) {
                    frontier.add(key);
                }
            }
        }
    }
    
    private List<Position> getPassageNeighbors(Position cell, Set<String> passages, int size) {
        List<Position> neighbors = new ArrayList<>();
        
        for (int i = 0; i < 4; i++) {
            int nx = cell.getX() + DX[i];
            int ny = cell.getY() + DY[i];
            String key = nx + "," + ny;
            
            if (nx >= 0 && nx < size && ny >= 0 && ny < size) {
                if (passages.contains(key)) {
                    neighbors.add(new Position(nx, ny));
                }
            }
        }
        return neighbors;
    }
    
    private List<Position> getNonPassageNeighbors(Position cell, Set<String> passages, int size) {
        List<Position> neighbors = new ArrayList<>();
        
        for (int i = 0; i < 4; i++) {
            int nx = cell.getX() + DX[i];
            int ny = cell.getY() + DY[i];
            String key = nx + "," + ny;
            
            if (nx > 0 && nx < size - 1 && ny > 0 && ny < size - 1) {
                if (!passages.contains(key)) {
                    neighbors.add(new Position(nx, ny));
                }
            }
        }
        return neighbors;
    }
    
    private String pickRandomFromSet(Set<String> set) {
        int index = random.nextInt(set.size());
        int i = 0;
        for (String item : set) {
            if (i == index) {
                return item;
            }
            i++;
        }
        return set.iterator().next();
    }
    
    private Position parsePosition(String key) {
        String[] parts = key.split(",");
        return new Position(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }
    
    private Position placeGoalAtDepth(Maze maze, List<Position> starts, int size) {
        int minDepth = size * 2;
        
        List<Position> walkableCells = new ArrayList<>();
        for (int i = 1; i < size - 1; i++) {
            for (int j = 1; j < size - 1; j++) {
                MazeCell cell = maze.getCell(i, j);
                if (cell.getCode() == 3) {
                    walkableCells.add(new Position(i, j));
                }
            }
        }
        
        List<Position> candidates = new ArrayList<>();
        
        for (Position cell : walkableCells) {
            boolean validForAllStarts = true;
            
            for (Position start : starts) {
                int dist = calculateShortestPath(maze, start, cell);
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
        
        Position goal = candidates.get(random.nextInt(candidates.size()));
        maze.setCell(goal.getX(), goal.getY(), 9);
        return goal;
    }
    
    private int calculateShortestPath(Maze maze, Position start, Position goal) {
        Queue<Position> queue = new LinkedList<>();
        Map<String, Integer> distance = new HashMap<>();
        
        String startKey = start.getX() + "," + start.getY();
        queue.add(start);
        distance.put(startKey, 0);
        
        while (!queue.isEmpty()) {
            Position current = queue.poll();
            String currentKey = current.getX() + "," + current.getY();
            int currentDist = distance.get(currentKey);
            
            if (current.equals(goal)) {
                return currentDist;
            }
            
            for (int i = 0; i < 4; i++) {
                int nx = current.getX() + DX[i];
                int ny = current.getY() + DY[i];
                String key = nx + "," + ny;
                
                if (maze.isValidPosition(nx, ny) && !distance.containsKey(key)) {
                    MazeCell cell = maze.getCell(nx, ny);
                    if (cell.isWalkable() || cell.getCode() == 9 || cell.getCode() == 0) {
                        distance.put(key, currentDist + 1);
                        queue.add(new Position(nx, ny));
                    }
                }
            }
        }
        
        return -1;
    }
    
    private boolean validateMinimumPathLength(Maze maze, int size) {
        int minimumRequired = size * 2;
        
        List<Position> starts = new ArrayList<>();
        Position goal = null;
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                MazeCell cell = maze.getCell(i, j);
                if (cell.getCode() == 0) {
                    starts.add(new Position(i, j));
                } else if (cell.getCode() == 9) {
                    goal = new Position(i, j);
                }
            }
        }
        
        if (goal == null || starts.isEmpty()) {
            return false;
        }
        
        for (Position start : starts) {
            int pathLength = calculateShortestPath(maze, start, goal);
            if (pathLength < 0 || pathLength < minimumRequired) {
                return false;
            }
        }
        
        return true;
    }
    
    private void placeItems(Maze maze, int size) {
        int itemCount = size / 3;
        
        for (int i = 0; i < itemCount; i++) {
            int attempts = 0;
            while (attempts < 50) {
                int x = 1 + random.nextInt(size - 2);
                int y = 1 + random.nextInt(size - 2);
                
                MazeCell cell = maze.getCell(x, y);
                if (cell.getCode() == 3) {
                    maze.setCell(x, y, 6);
                    break;
                }
                attempts++;
            }
        }
    }
    
    private void initializeFog(Maze maze, int size) {
    }
    
    private Maze generateFallbackMaze(int size) {
        Maze maze = new Maze(size);
        
        initializeAllWalls(maze, size);
        List<Position> starts = placeStartPoints(maze, size);
        generateMazeWithPrims(maze, starts, size);
        
        List<Position> walkableCells = new ArrayList<>();
        for (int i = 1; i < size - 1; i++) {
            for (int j = 1; j < size - 1; j++) {
                MazeCell cell = maze.getCell(i, j);
                if (cell.getCode() == 3) {
                    walkableCells.add(new Position(i, j));
                }
            }
        }
        
        Position bestGoal = null;
        int bestMinDist = -1;
        
        for (Position cell : walkableCells) {
            int minDistFromStarts = Integer.MAX_VALUE;
            boolean reachable = true;
            
            for (Position start : starts) {
                int dist = calculateShortestPath(maze, start, cell);
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
            maze.setCell(bestGoal.getX(), bestGoal.getY(), 9);
        } else if (!walkableCells.isEmpty()) {
            Position goal = walkableCells.get(random.nextInt(walkableCells.size()));
            maze.setCell(goal.getX(), goal.getY(), 9);
        }
        
        placeItems(maze, size);
        initializeFog(maze, size);
        
        return maze;
    }
}
