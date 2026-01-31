package com.example.secuproject;

import java.util.*;

/**
 * Enemy의 경로 기억 시스템
 * LinkedList로 이동 경로를 저장하고, 막다른 길을 기억합니다.
 */
public class EnemyMemory {

    /**
     * 위치 정보를 담는 내부 클래스
     */
    public static class Position {
        public final int x;
        public final int y;

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Position) {
                Position other = (Position) obj;
                return this.x == other.x && this.y == other.y;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        @Override
        public String toString() {
            return "(" + x + "," + y + ")";
        }
    }

    // 이동 경로 기록 (LinkedList로 순서 보장)
    private LinkedList<Position> pathHistory;

    // 탐색한 셀의 실제 값 기억 (시야에 들어온 셀만)
    private Map<Position, Integer> exploredMap;

    // 막다른 길로 확인된 위치들
    private Set<Position> deadEnds;

    // 각 위치별 방문 횟수
    private Map<Position, Integer> visitCount;

    public EnemyMemory() {
        this.pathHistory = new LinkedList<>();
        this.exploredMap = new HashMap<>();
        this.deadEnds = new HashSet<>();
        this.visitCount = new HashMap<>();
    }

    /**
     * 현재 위치를 경로에 추가
     */
    public void recordPosition(int x, int y) {
        Position pos = new Position(x, y);
        pathHistory.addLast(pos);

        // 방문 횟수 증가
        int count = visitCount.getOrDefault(pos, 0) + 1;
        visitCount.put(pos, count);
    }

    /**
     * 시야 내 셀 정보를 기억에 저장
     * @param x 셀 X 좌표
     * @param y 셀 Y 좌표
     * @param cellValue 셀 값 (3=길, 4=벽, 9=골 등)
     */
    public void rememberCell(int x, int y, int cellValue) {
        exploredMap.put(new Position(x, y), cellValue);
    }

    /**
     * 현재 위치를 막다른 길로 마킹
     */
    public void markAsDeadEnd(int x, int y) {
        deadEnds.add(new Position(x, y));
    }

    /**
     * 해당 위치가 막다른 길인지 확인
     */
    public boolean isDeadEnd(int x, int y) {
        return deadEnds.contains(new Position(x, y));
    }

    /**
     * 해당 위치를 탐색한 적 있는지 확인
     */
    public boolean hasExplored(int x, int y) {
        return exploredMap.containsKey(new Position(x, y));
    }

    /**
     * 기억된 셀 값 조회 (탐색 안 한 경우 -1 반환)
     */
    public int getRememberedCell(int x, int y) {
        Position pos = new Position(x, y);
        return exploredMap.getOrDefault(pos, -1);
    }

    /**
     * 해당 위치의 방문 횟수 조회
     */
    public int getVisitCount(int x, int y) {
        return visitCount.getOrDefault(new Position(x, y), 0);
    }

    /**
     * 백트래킹: 직전 위치 반환 (현재 위치 제거)
     * @return 이전 위치, 없으면 null
     */
    public Position backtrack() {
        if (pathHistory.size() > 1) {
            pathHistory.removeLast(); // 현재 위치 제거
            return pathHistory.peekLast(); // 이전 위치 반환
        }
        return null;
    }

    /**
     * 경로 히스토리 크기
     */
    public int getPathSize() {
        return pathHistory.size();
    }

    /**
     * 마지막 위치 조회
     */
    public Position getLastPosition() {
        return pathHistory.isEmpty() ? null : pathHistory.peekLast();
    }

    /**
     * 전체 경로 조회 (디버깅용)
     */
    public List<Position> getFullPath() {
        return new ArrayList<>(pathHistory);
    }

    /**
     * 메모리 초기화
     */
    public void clear() {
        pathHistory.clear();
        exploredMap.clear();
        deadEnds.clear();
        visitCount.clear();
    }

    /**
     * 탐색 통계 출력 (디버깅용)
     */
    public String getStats() {
        return String.format("탐색: %d셀, 막다른길: %d개, 경로: %d칸",
            exploredMap.size(), deadEnds.size(), pathHistory.size());
    }
}
