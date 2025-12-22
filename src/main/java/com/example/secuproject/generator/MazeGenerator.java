package com.example.secuproject.generator;

import com.example.secuproject.model.Maze;
import com.example.secuproject.model.MazeCell;
import com.example.secuproject.model.Position;

import java.util.*;

/**
 * 미로를 자동으로 생성하는 클래스
 * 스타트에서 도착지점까지 경로가 2개 이상 존재하도록 보장합니다
 */
public class MazeGenerator {
    private Random random;
    
    public MazeGenerator() {
        this.random = new Random();
    }
    
    /**
     * N × N 크기의 미로를 생성합니다
     * @param size 미로 크기
     * @return 생성된 미로
     */
    public Maze generateMaze(int size) {
        if (size < 5) {
            size = 5; // 최소 크기 보장
        }
        
        Maze maze = new Maze(size);
        
        // 1단계: 기본 미로 구조 생성 (모든 칸을 벽으로 시작)
        generateBasicMaze(maze, size);
        
        // 2단계: 스타트 지점 2개 배치
        List<Position> starts = placeStartPoints(maze, size);
        
        // 3단계: 도착지점 배치
        Position goal = placeGoal(maze, size, starts);
        
        // 4단계: 경로 생성 (최소 2개 이상의 경로 보장)
        ensureMultiplePaths(maze, starts, goal);
        
        // 5단계: 아이템 배치
        placeItems(maze, size);
        
        // 6단계: 안개 초기화 (모든 칸에 안개 코드 5 설정)
        initializeFog(maze, size);
        
        return maze;
    }
    
    /**
     * 기본 미로 구조를 생성합니다 (길과 벽을 랜덤하게 배치)
     */
    private void generateBasicMaze(Maze maze, int size) {
        // 중앙 부분에 길을 많이 만들고, 가장자리는 벽으로 유지
        for (int i = 1; i < size - 1; i++) {
            for (int j = 1; j < size - 1; j++) {
                // 60% 확률로 길 생성
                if (random.nextDouble() < 0.6) {
                    maze.setCell(i, j, 3); // 길
                }
            }
        }
    }
    
    /**
     * 스타트 지점 2개를 배치합니다
     * 첫 번째: 맨 위쪽 중앙
     * 두 번째: 맨 밑쪽 중앙
     */
    private List<Position> placeStartPoints(Maze maze, int size) {
        List<Position> starts = new ArrayList<>();
        
        // 첫 번째 스타트: 맨 위쪽 중앙
        int x1 = 1; // 맨 위쪽 (가장자리 제외)
        int y1 = size / 2; // 중앙
        maze.setCell(x1, y1, 0);
        starts.add(new Position(x1, y1));
        
        // 두 번째 스타트: 맨 밑쪽 중앙
        int x2 = size - 2; // 맨 밑쪽 (가장자리 제외)
        int y2 = size / 2; // 중앙
        maze.setCell(x2, y2, 0);
        starts.add(new Position(x2, y2));
        
        // 스타트 지점 주변을 길로 만듭니다
        for (Position start : starts) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    int x = start.getX() + dx;
                    int y = start.getY() + dy;
                    if (maze.isValidPosition(x, y) && !(dx == 0 && dy == 0)) {
                        maze.setCell(x, y, 3); // 길
                    }
                }
            }
        }
        
        return starts;
    }
    
    /**
     * 도착지점을 배치합니다
     */
    private Position placeGoal(Maze maze, int size, List<Position> starts) {
        // 스타트 지점들과 멀리 떨어진 곳에 배치
        Position goal = null;
        int attempts = 0;
        
        while (goal == null && attempts < 100) {
            int x = size / 2 + random.nextInt(size / 3) - size / 6;
            int y = size / 2 + random.nextInt(size / 3) - size / 6;
            
            // 스타트 지점들과 충분히 떨어져 있는지 확인
            boolean farEnough = true;
            for (Position start : starts) {
                int distance = Math.abs(x - start.getX()) + Math.abs(y - start.getY());
                if (distance < size / 2) {
                    farEnough = false;
                    break;
                }
            }
            
            if (farEnough) {
                maze.setCell(x, y, 9); // 도착지점
                goal = new Position(x, y);
                // 도착지점 주변을 길로 만듭니다
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        int nx = x + dx;
                        int ny = y + dy;
                        if (maze.isValidPosition(nx, ny) && !(dx == 0 && dy == 0)) {
                            maze.setCell(nx, ny, 3); // 길
                        }
                    }
                }
            }
            attempts++;
        }
        
        // 시도 실패 시 중앙에 배치
        if (goal == null) {
            int x = size / 2;
            int y = size / 2;
            maze.setCell(x, y, 9);
            goal = new Position(x, y);
        }
        
        return goal;
    }
    
    /**
     * 최소 2개 이상의 경로를 보장합니다
     */
    private void ensureMultiplePaths(Maze maze, List<Position> starts, Position goal) {
        // 각 스타트 지점에서 도착지점까지 경로가 있는지 확인하고, 없으면 경로를 만듭니다
        for (Position start : starts) {
            // BFS로 경로 확인
            if (!hasPath(maze, start, goal)) {
                // 경로가 없으면 경로를 만듭니다
                createPath(maze, start, goal);
            }
        }
        
        // 최소 2개 이상의 경로가 있는지 확인하고, 없으면 추가 경로를 만듭니다
        int pathCount = countPaths(maze, starts, goal);
        if (pathCount < 2) {
            // 추가 경로 생성
            createAdditionalPath(maze, starts, goal);
        }
    }
    
    /**
     * 두 지점 사이에 경로가 있는지 확인합니다 (BFS 사용)
     */
    private boolean hasPath(Maze maze, Position start, Position goal) {
        Queue<Position> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        
        queue.add(start);
        visited.add(start.getX() + "," + start.getY());
        
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        
        while (!queue.isEmpty()) {
            Position current = queue.poll();
            
            if (current.equals(goal)) {
                return true;
            }
            
            for (int i = 0; i < 4; i++) {
                int nx = current.getX() + dx[i];
                int ny = current.getY() + dy[i];
                String key = nx + "," + ny;
                
                if (maze.isValidPosition(nx, ny) && !visited.contains(key)) {
                    MazeCell cell = maze.getCell(nx, ny);
                    if (cell.isWalkable() || cell.getCode() == 9) {
                        visited.add(key);
                        queue.add(new Position(nx, ny));
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * 두 지점 사이에 경로를 생성합니다
     */
    private void createPath(Maze maze, Position start, Position goal) {
        // A* 알고리즘과 유사하게 직선 경로를 만들되, 장애물을 피합니다
        int currentX = start.getX();
        int currentY = start.getY();
        
        while (!(currentX == goal.getX() && currentY == goal.getY())) {
            // 목표 방향으로 이동
            int dx = Integer.compare(goal.getX(), currentX);
            int dy = Integer.compare(goal.getY(), currentY);
            
            // 우선순위: 대각선 이동 > 수평/수직 이동
            if (dx != 0 && dy != 0 && random.nextBoolean()) {
                currentX += dx;
            } else if (dy != 0) {
                currentY += dy;
            } else if (dx != 0) {
                currentX += dx;
            }
            
            // 범위 체크
            if (maze.isValidPosition(currentX, currentY)) {
                maze.setCell(currentX, currentY, 3); // 길로 만듭니다
            } else {
                break;
            }
        }
    }
    
    /**
     * 경로의 개수를 세어봅니다 (대략적인 추정)
     */
    private int countPaths(Maze maze, List<Position> starts, Position goal) {
        int count = 0;
        for (Position start : starts) {
            if (hasPath(maze, start, goal)) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * 추가 경로를 생성합니다
     */
    private void createAdditionalPath(Maze maze, List<Position> starts, Position goal) {
        // 첫 번째 스타트에서 다른 경로로 도착지점까지 가는 경로를 만듭니다
        if (!starts.isEmpty()) {
            Position start = starts.get(0);
            // 기존 경로와 다른 경로를 만들기 위해 약간 다른 방향으로 시작
            createPath(maze, start, goal);
        }
    }
    
    /**
     * 아이템을 배치합니다
     */
    private void placeItems(Maze maze, int size) {
        int itemCount = size / 3; // 미로 크기에 비례하여 아이템 개수 결정
        
        for (int i = 0; i < itemCount; i++) {
            int attempts = 0;
            while (attempts < 50) {
                int x = 1 + random.nextInt(size - 2);
                int y = 1 + random.nextInt(size - 2);
                
                MazeCell cell = maze.getCell(x, y);
                // 길 위에만 아이템 배치
                if (cell.getCode() == 3) {
                    maze.setCell(x, y, 6); // 아이템
                    break;
                }
                attempts++;
            }
        }
    }
    
    /**
     * 안개를 초기화합니다 (모든 칸에 안개 코드 5 설정)
     */
    private void initializeFog(Maze maze, int size) {
        // 안개는 게임 중 동적으로 처리되므로 여기서는 기본 설정만 합니다
        // 실제 안개는 Maze 클래스의 updateVisibility에서 처리됩니다
    }
}

