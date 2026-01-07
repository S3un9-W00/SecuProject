package com.example.secuproject;

import java.util.*;

public class Enemy {
    // 방향 상수: 0=위, 1=오른쪽, 2=아래, 3=왼쪽
    private int dir = 1; // 처음에는 오른쪽을 본다고 가정
    private int x;
    private int y;
    private Maze_two maze;
    private int[][] map;
    private Map<String, Integer> visitedCount; // 방문 횟수 추적 (루프 방지)
    private int samePositionCount = 0; // 같은 위치에 머무는 횟수
    private int lastX = -1, lastY = -1; // 이전 위치

    public Enemy(Maze_two maze, int startX, int startY, int[][] map) {
        this.maze = maze;
        this.x = startX;
        this.y = startY;
        this.map = map;
        this.visitedCount = new HashMap<>();
    }

    // 한 칸만 오른손 법칙으로 이동 (루프 방지 포함)
    public void step() {
        // 방향별 dx, dy
        int[] dx = {-1, 0, 1, 0};
        int[] dy = {0, 1, 0, -1};

        // 같은 위치에 머무는지 확인
        if (x == lastX && y == lastY) {
            samePositionCount++;
        } else {
            samePositionCount = 0;
        }
        lastX = x;
        lastY = y;

        // 같은 위치를 3번 이상 방문하면 루프로 간주
        String currentPos = x + "," + y;
        int visitCount = visitedCount.getOrDefault(currentPos, 0) + 1;
        visitedCount.put(currentPos, visitCount);

        // 루프 감지: 같은 위치를 5번 이상 방문하거나, 같은 위치에 3번 이상 머무르면
        if (visitCount > 5 || samePositionCount > 3) {
            // 루프 탈출: 이동 가능한 모든 방향 중 랜덤 선택
            List<int[]> possibleMoves = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                int nx = x + dx[i];
                int ny = y + dy[i];
                if (isFree(nx, ny)) {
                    // 방문 횟수가 적은 곳 우선
                    String nextPos = nx + "," + ny;
                    int nextVisitCount = visitedCount.getOrDefault(nextPos, 0);
                    possibleMoves.add(new int[]{nx, ny, i, nextVisitCount});
                }
            }
            
            if (!possibleMoves.isEmpty()) {
                // 방문 횟수가 적은 곳 우선 선택, 같으면 랜덤
                possibleMoves.sort((a, b) -> Integer.compare(a[3], b[3]));
                Random random = new Random();
                int[] move = possibleMoves.get(random.nextInt(Math.min(possibleMoves.size(), 3)));
                x = move[0];
                y = move[1];
                dir = move[2];
                visitedCount.clear(); // 방문 기록 초기화
                samePositionCount = 0;
                return;
            }
        }

        // 일반적인 오른손 법칙 (방문 횟수 고려)
        // 1. 오른쪽
        int rightDir = (dir + 1) % 4;
        int rx = x + dx[rightDir];
        int ry = y + dy[rightDir];

        if (isFree(rx, ry) && getVisitCount(rx, ry) < 3) {
            dir = rightDir;
            x = rx;
            y = ry;
            return;
        }

        // 2. 앞
        int fx = x + dx[dir];
        int fy = y + dy[dir];
        if (isFree(fx, fy) && getVisitCount(fx, fy) < 3) {
            x = fx;
            y = fy;
            return;
        }

        // 3. 왼쪽
        int leftDir = (dir + 3) % 4;
        int lx = x + dx[leftDir];
        int ly = y + dy[leftDir];
        if (isFree(lx, ly) && getVisitCount(lx, ly) < 3) {
            dir = leftDir;
            x = lx;
            y = ly;
            return;
        }

        // 4. 뒤 (유턴) - 방문 횟수 체크 없이 이동 (막다른 길일 수 있음)
        int backDir = (dir + 2) % 4;
        int bx = x + dx[backDir];
        int by = y + dy[backDir];
        if (isFree(bx, by)) {
            dir = backDir;
            x = bx;
            y = by;
        }
    }
    
    /**
     * 특정 위치의 방문 횟수를 가져옵니다
     */
    private int getVisitCount(int x, int y) {
        String pos = x + "," + y;
        return visitedCount.getOrDefault(pos, 0);
    }

    private boolean isFree(int nx, int ny) {
        if (nx < 0 || nx >= map.length || ny < 0 || ny >= map[0].length) {
            return false;
        }
        // 4가 벽이라는 건 너 코드 기준 그대로
        return map[nx][ny] != 4;
    }

    public int getX() { return x; }
    public int getY() { return y; }
}
