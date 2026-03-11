package com.example.secuproject;

import java.util.*;

public class Enemy {
    private static final int[] DX = {-1, 0, 1, 0};
    private static final int[] DY = {0, 1, 0, -1};

    private int dir = 1;
    private int x;
    private int y;
    private Maze_two maze;
    private int[][] map;

    private int viewRange = 1;

    private EnemyMemory memory;

    private int stuckCount = 0;
    private EnemyMemory.Position lastPosition = null;

    private boolean hasHammer = false;
    private boolean hammerUsed = false;
    private long immobilizedUntil = 0;
    private long torchEndTime = 0;

    public Enemy(Maze_two maze, int startX, int startY, int[][] map) {
        this.maze = maze;
        this.x = startX;
        this.y = startY;
        this.map = map;
        this.memory = new EnemyMemory();

        memory.recordPosition(x, y);
        updateVision();
    }

    private void updateVision() {
        for (int i = x - viewRange; i <= x + viewRange; i++) {
            for (int j = y - viewRange; j <= y + viewRange; j++) {
                if (isInBounds(i, j)) {
                    memory.rememberCell(i, j, map[i][j]);
                }
            }
        }
    }

    private boolean isInBounds(int nx, int ny) {
        return nx >= 0 && nx < map.length && ny >= 0 && ny < map[0].length;
    }

    private boolean canMove(int nx, int ny) {
        if (!isInBounds(nx, ny)) {
            return false;
        }

        if (memory.isDeadEnd(nx, ny)) {
            return false;
        }

        boolean inView = Math.abs(nx - x) <= viewRange && Math.abs(ny - y) <= viewRange;

        if (inView) {
            return map[nx][ny] != 4;
        } else {
            int remembered = memory.getRememberedCell(nx, ny);
            if (remembered == -1) {
                return true;
            }
            return remembered != 4;
        }
    }

    private boolean canMoveWithHammer(int nx, int ny) {
        if (!isInBounds(nx, ny)) {
            return false;
        }

        if (memory.isDeadEnd(nx, ny)) {
            return false;
        }

        boolean inView = Math.abs(nx - x) <= viewRange && Math.abs(ny - y) <= viewRange;

        if (inView) {
            if (map[nx][ny] == 4) {
                return hasHammer && !hammerUsed;
            }
            return map[nx][ny] != 4;
        } else {
            int remembered = memory.getRememberedCell(nx, ny);
            if (remembered == -1) {
                return true;
            }
            if (remembered == 4) {
                return hasHammer && !hammerUsed;
            }
            return remembered != 4;
        }
    }

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

    public void step() {
        long now = System.currentTimeMillis();

        if (now < immobilizedUntil) {
            return;
        }

        if (torchEndTime > 0 && now > torchEndTime) {
            viewRange = 1;
            torchEndTime = 0;
        }

        updateVision();

        EnemyMemory.Position currentPos = new EnemyMemory.Position(x, y);
        if (currentPos.equals(lastPosition)) {
            stuckCount++;
        } else {
            stuckCount = 0;
        }
        lastPosition = currentPos;

        if (isDeadEnd() && stuckCount > 2) {
            memory.markAsDeadEnd(x, y);
        }

        Integer nextDir = decideDirection();

        if (nextDir != null) {
            int nx = x + DX[nextDir];
            int ny = y + DY[nextDir];

            if (isInBounds(nx, ny) && map[nx][ny] == 4 && hasHammer && !hammerUsed) {
                map[nx][ny] = 3;
                hammerUsed = true;
                hasHammer = false;
            }

            dir = nextDir;
            x = x + DX[dir];
            y = y + DY[dir];

            int currentCell = map[x][y];
            if (currentCell == 6) {
                map[x][y] = 3;
                viewRange = 2;
                torchEndTime = System.currentTimeMillis() + 10_000;
            } else if (currentCell == 7) {
                map[x][y] = 3;
                hasHammer = true;
                hammerUsed = false;
            } else if (currentCell == 8) {
                map[x][y] = 3;
                immobilizedUntil = System.currentTimeMillis() + 3_000;
            }

            memory.recordPosition(x, y);
        }
    }

    private Integer decideDirection() {
        List<Integer> unexplored = getUnexploredDirections();
        if (!unexplored.isEmpty()) {
            return selectByRightHandRule(unexplored);
        }

        List<int[]> candidates = new ArrayList<>();
        for (int d = 0; d < 4; d++) {
            int nx = x + DX[d];
            int ny = y + DY[d];
            if (canMove(nx, ny)) {
                int visits = memory.getVisitCount(nx, ny);
                candidates.add(new int[]{d, visits});
            }
        }

        if (candidates.isEmpty()) {
            if (hasHammer && !hammerUsed) {
                for (int d = 0; d < 4; d++) {
                    int nx = x + DX[d];
                    int ny = y + DY[d];
                    if (isInBounds(nx, ny) && map[nx][ny] == 4) {
                        return d;
                    }
                }
            }
            return null;
        }

        candidates.sort(Comparator.comparingInt(a -> a[1]));

        int minVisits = candidates.get(0)[1];
        List<Integer> minVisitDirs = new ArrayList<>();
        for (int[] c : candidates) {
            if (c[1] == minVisits) {
                minVisitDirs.add(c[0]);
            }
        }

        return selectByRightHandRule(minVisitDirs);
    }

    private Integer selectByRightHandRule(List<Integer> directions) {
        int[] priority = {
            (dir + 1) % 4,
            dir,
            (dir + 3) % 4,
            (dir + 2) % 4
        };

        for (int p : priority) {
            if (directions.contains(p)) {
                return p;
            }
        }

        return directions.isEmpty() ? null : directions.get(0);
    }

    public void setViewRange(int range) {
        this.viewRange = range;
    }

    public int getViewRange() {
        return viewRange;
    }

    public boolean hasHammer() {
        return hasHammer && !hammerUsed;
    }

    public boolean isImmobilized() {
        return System.currentTimeMillis() < immobilizedUntil;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public String getMemoryStats() {
        return memory.getStats();
    }

    public EnemyMemory getMemory() {
        return memory;
    }
}
