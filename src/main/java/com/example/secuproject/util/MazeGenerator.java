package com.example.secuproject.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * 미로 제네레이터
 * 랜덤 미로를 생성합니다
 */
public class MazeGenerator {
    private Random random;
    
    public MazeGenerator() {
        this.random = new Random();
    }
    
    /**
     * N × N 크기의 랜덤 미로를 생성합니다
     * @param size 미로 크기
     * @return 생성된 미로 맵 (2차원 배열)
     */
    public int[][] generateMaze(int size) {
        if (size < 5) {
            size = 5; // 최소 크기
        }
        
        int[][] map = new int[size][size];
        
        // 1. 모든 칸을 벽(4)으로 초기화
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                map[i][j] = 4;
            }
        }
        
        // 2. 중앙 부분에 길(3)을 랜덤하게 생성
        for (int i = 1; i < size - 1; i++) {
            for (int j = 1; j < size - 1; j++) {
                if (random.nextDouble() < 0.6) { // 60% 확률로 길 생성
                    map[i][j] = 3;
                }
            }
        }
        
        // 3. 스타트 지점 2개 배치 (맨 위쪽 중앙, 맨 아래쪽 중앙)
        int start1X = 1;
        int start1Y = size / 2;
        int start2X = size - 2;
        int start2Y = size / 2;
        
        map[start1X][start1Y] = 0;
        map[start2X][start2Y] = 0;
        
        // 스타트 지점 주변을 길로 만들기
        makePathAround(map, start1X, start1Y, size);
        makePathAround(map, start2X, start2Y, size);
        
        // 4. 도착지점 배치 (중앙 근처)
        int goalX = size / 2;
        int goalY = size / 2;
        map[goalX][goalY] = 9;
        makePathAround(map, goalX, goalY, size);
        
        // 5. 스타트에서 도착지점까지 경로 보장
        ensurePath(map, start1X, start1Y, goalX, goalY, size);
        ensurePath(map, start2X, start2Y, goalX, goalY, size);
        
        // 6. 아이템 배치
        placeItems(map, size);
        
        return map;
    }
    
    /**
     * 특정 위치 주변을 길로 만듭니다
     */
    private void makePathAround(int[][] map, int x, int y, int size) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int nx = x + dx;
                int ny = y + dy;
                if (nx >= 1 && nx < size - 1 && ny >= 1 && ny < size - 1) {
                    if (map[nx][ny] == 4) {
                        map[nx][ny] = 3;
                    }
                }
            }
        }
    }
    
    /**
     * 두 지점 사이에 경로를 보장합니다
     */
    private void ensurePath(int[][] map, int startX, int startY, int goalX, int goalY, int size) {
        // BFS로 경로 확인
        if (!hasPath(map, startX, startY, goalX, goalY, size)) {
            // 경로가 없으면 경로 생성
            createPath(map, startX, startY, goalX, goalY, size);
        }
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
     * 두 지점 사이에 경로를 생성합니다
     */
    private void createPath(int[][] map, int startX, int startY, int goalX, int goalY, int size) {
        int currentX = startX;
        int currentY = startY;
        
        while (!(currentX == goalX && currentY == goalY)) {
            // 목표 방향으로 이동
            int dx = Integer.compare(goalX, currentX);
            int dy = Integer.compare(goalY, currentY);
            
            if (dx != 0) {
                currentX += dx;
            } else if (dy != 0) {
                currentY += dy;
            }
            
            if (currentX >= 0 && currentX < size && currentY >= 0 && currentY < size) {
                if (map[currentX][currentY] == 4) {
                    map[currentX][currentY] = 3; // 벽을 길로 변경
                }
            } else {
                break;
            }
        }
    }
    
    /**
     * 아이템을 배치합니다
     */
    private void placeItems(int[][] map, int size) {
        int itemCount = size / 3; // 미로 크기에 비례
        
        for (int i = 0; i < itemCount; i++) {
            int attempts = 0;
            while (attempts < 50) {
                int x = 1 + random.nextInt(size - 2);
                int y = 1 + random.nextInt(size - 2);
                
                // 길 위에만 아이템 배치
                if (map[x][y] == 3) {
                    map[x][y] = 6; // 아이템
                    break;
                }
                attempts++;
            }
        }
    }
    
    /**
     * 미로를 텍스트 파일로 저장합니다
     */
    public void saveToFile(int[][] map, String filePath) throws IOException {
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        
        // 부모 디렉토리가 있고 존재하지 않으면 생성
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
    
    /**
     * 메인 함수 - 미로를 생성하고 파일로 저장
     */
    public static void main(String[] args) {
        MazeGenerator generator = new MazeGenerator();
        MazeValidator validator = new MazeValidator();
        
        int size = 10; // 기본 크기
        if (args.length > 0) {
            try {
                size = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("잘못된 크기입니다. 기본 크기 10을 사용합니다.");
            }
        }
        
        String filePath = "maze.txt"; // 기본 파일명
        if (args.length > 1) {
            filePath = args[1];
        }
        
        System.out.println("미로 생성 중... (크기: " + size + "x" + size + ")");
        
        // 미로 생성 및 검증
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
        
        // 파일로 저장
        try {
            generator.saveToFile(map, filePath);
            System.out.println("미로가 저장되었습니다: " + filePath);
        } catch (IOException e) {
            System.out.println("파일 저장 중 오류 발생: " + e.getMessage());
        }
    }
}

