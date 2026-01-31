package com.example.secuproject;

import java.util.*;

public class Enemy {
    // 방향 상수: 0=위, 1=오른쪽, 2=아래, 3=왼쪽
    private static final int[] DX = {-1, 0, 1, 0};
    private static final int[] DY = {0, 1, 0, -1};

    private int dir = 1; // 현재 바라보는 방향 (처음엔 오른쪽)
    private int x;
    private int y;
    private Maze_two maze;
    private int[][] map;

    // 시야 범위 (플레이어와 동일하게 적용)
    private int viewRange = 1; // 1이면 3x3, 2이면 5x5

    // 메모리 시스템 (LinkedList 기반 경로 기억)
    private EnemyMemory memory;

    // 막다른 길 감지용
    private int stuckCount = 0;
    private EnemyMemory.Position lastPosition = null;

    public Enemy(Maze_two maze, int startX, int startY, int[][] map) {
        this.maze = maze;
        this.x = startX;
        this.y = startY;
        this.map = map;
        this.memory = new EnemyMemory();

        // 시작 위치 기록
        memory.recordPosition(x, y);
        updateVision(); // 초기 시야 정보 저장
    }

    /**
     * 현재 시야 내의 셀 정보를 메모리에 저장
     */
    private void updateVision() {
        for (int i = x - viewRange; i <= x + viewRange; i++) {
            for (int j = y - viewRange; j <= y + viewRange; j++) {
                if (isInBounds(i, j)) {
                    memory.rememberCell(i, j, map[i][j]);
                }
            }
        }
    }

    /**
     * 해당 좌표가 맵 범위 내인지 확인
     */
    private boolean isInBounds(int nx, int ny) {
        return nx >= 0 && nx < map.length && ny >= 0 && ny < map[0].length;
    }

    /**
     * 시야 내에서 이동 가능한지 확인 (안개 적용)
     * - 시야 밖: 기억에 있으면 기억 기반, 없으면 이동 불가
     * - 시야 내: 실제 맵 값 확인
     */
    private boolean canMove(int nx, int ny) {
        if (!isInBounds(nx, ny)) {
            return false;
        }

        // 막다른 길로 기억된 곳은 피함
        if (memory.isDeadEnd(nx, ny)) {
            return false;
        }

        // 시야 내인지 확인
        boolean inView = Math.abs(nx - x) <= viewRange && Math.abs(ny - y) <= viewRange;

        if (inView) {
            // 시야 내: 실제 맵 값 확인 (벽이 아니면 이동 가능)
            return map[nx][ny] != 4;
        } else {
            // 시야 밖: 기억에 있으면 기억 기반 판단
            int remembered = memory.getRememberedCell(nx, ny);
            if (remembered == -1) {
                // 탐색 안 한 곳 = 미지의 영역 (일단 이동 시도 가능)
                return true;
            }
            return remembered != 4; // 벽으로 기억되면 불가
        }
    }

    /**
     * 미탐색 영역이 있는 방향 찾기
     */
    private List<Integer> getUnexploredDirections() {
        List<Integer> unexplored = new ArrayList<>();
        for (int d = 0; d < 4; d++) {
            int nx = x + DX[d];
            int ny = y + DY[d];
            if (isInBounds(nx, ny) && !memory.hasExplored(nx, ny) && canMove(nx, ny)) {
                unexplored.add(d);
            }
        }
        return unexplored;
    }

    /**
     * 막다른 길인지 확인 (이동 가능한 방향이 1개 이하)
     */
    private boolean isDeadEnd() {
        int movableCount = 0;
        for (int d = 0; d < 4; d++) {
            int nx = x + DX[d];
            int ny = y + DY[d];
            if (canMove(nx, ny)) {
                movableCount++;
            }
        }
        return movableCount <= 1;
    }

    /**
     * 한 칸 이동 (메모리 기반 스마트 탐색)
     */
    public void step() {
        // 1. 시야 업데이트 (현재 보이는 영역 기억)
        updateVision();

        // 2. 같은 위치에 계속 있는지 확인 (stuck 감지)
        EnemyMemory.Position currentPos = new EnemyMemory.Position(x, y);
        if (currentPos.equals(lastPosition)) {
            stuckCount++;
        } else {
            stuckCount = 0;
        }
        lastPosition = currentPos;

        // 3. 막다른 길 감지 및 마킹
        if (isDeadEnd() && stuckCount > 2) {
            memory.markAsDeadEnd(x, y);
        }

        // 4. 이동 방향 결정 (우선순위 기반)
        Integer nextDir = decideDirection();

        if (nextDir != null) {
            // 이동 실행
            dir = nextDir;
            x = x + DX[dir];
            y = y + DY[dir];

            // 경로 기록
            memory.recordPosition(x, y);
        }
    }

    /**
     * 이동 방향 결정 (우선순위 기반)
     * 1. 미탐색 영역 우선
     * 2. 방문 횟수 적은 곳 우선
     * 3. 오른손 법칙 (fallback)
     */
    private Integer decideDirection() {
        // 우선순위 1: 미탐색 영역이 있는 방향
        List<Integer> unexplored = getUnexploredDirections();
        if (!unexplored.isEmpty()) {
            // 여러 개면 오른손 법칙 우선순위로 선택
            return selectByRightHandRule(unexplored);
        }

        // 우선순위 2: 이동 가능한 방향 중 방문 횟수 최소
        List<int[]> candidates = new ArrayList<>(); // {direction, visitCount}
        for (int d = 0; d < 4; d++) {
            int nx = x + DX[d];
            int ny = y + DY[d];
            if (canMove(nx, ny)) {
                int visits = memory.getVisitCount(nx, ny);
                candidates.add(new int[]{d, visits});
            }
        }

        if (candidates.isEmpty()) {
            // 이동 불가 - 막혔음
            return null;
        }

        // 방문 횟수 적은 순으로 정렬
        candidates.sort(Comparator.comparingInt(a -> a[1]));

        // 가장 적게 방문한 곳들 중 오른손 법칙 적용
        int minVisits = candidates.get(0)[1];
        List<Integer> minVisitDirs = new ArrayList<>();
        for (int[] c : candidates) {
            if (c[1] == minVisits) {
                minVisitDirs.add(c[0]);
            }
        }

        return selectByRightHandRule(minVisitDirs);
    }

    /**
     * 오른손 법칙 우선순위로 방향 선택
     * 우선순위: 오른쪽 > 앞 > 왼쪽 > 뒤
     */
    private Integer selectByRightHandRule(List<Integer> directions) {
        int[] priority = {
            (dir + 1) % 4, // 오른쪽
            dir,           // 앞
            (dir + 3) % 4, // 왼쪽
            (dir + 2) % 4  // 뒤
        };

        for (int p : priority) {
            if (directions.contains(p)) {
                return p;
            }
        }

        // fallback: 첫 번째 방향
        return directions.isEmpty() ? null : directions.get(0);
    }

    /**
     * 시야 범위 설정 (횃불 효과 등)
     */
    public void setViewRange(int range) {
        this.viewRange = range;
    }

    public int getViewRange() {
        return viewRange;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    /**
     * 메모리 통계 (디버깅용)
     */
    public String getMemoryStats() {
        return memory.getStats();
    }

    /**
     * 메모리 객체 반환 (테스트용)
     */
    public EnemyMemory getMemory() {
        return memory;
    }
}
