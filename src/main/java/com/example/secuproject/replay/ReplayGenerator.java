package com.example.secuproject.replay;

import com.example.secuproject.Maze_two;
import com.example.secuproject.log.GameEvent;
import com.example.secuproject.log.GameLog;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ReplayGenerator {

    private ReplayGenerator() {}

    public static List<ReplayFrame> generateFrames(GameLog log) {
        if (log == null || log.getInitialMaze() == null) {
            return List.of();
        }

        int[][] mazeCopy = deepCopy2D(log.getInitialMaze());
        Maze_two maze = new Maze_two(mazeCopy);

        List<GameEvent> events = new ArrayList<>(log.getEvents() == null ? List.of() : log.getEvents());
        events.sort(Comparator.comparingLong(GameEvent::getTimestamp));

        long baseTs = !events.isEmpty() ? events.get(0).getTimestamp() : log.getStartTime();

        List<ReplayFrame> frames = new ArrayList<>();

        // 초기 프레임
        ReplayFrame initial = new ReplayFrame();
        initial.index = 0;
        initial.offsetMillis = 0;
        initial.eventType = "START";
        initial.direction = ' ';
        initial.playerX = log.getInitialPlayerX();
        initial.playerY = log.getInitialPlayerY();
        initial.enemyX = log.getInitialEnemyX();
        initial.enemyY = log.getInitialEnemyY();
        initial.message = "리플레이 시작";
        initial.success = true;
        initial.mazeView = maze.showMazeNoFog(initial.enemyX, initial.enemyY);
        frames.add(initial);

        int enemyX = log.getInitialEnemyX();
        int enemyY = log.getInitialEnemyY();

        int idx = 1;
        for (GameEvent e : events) {
            if (e == null) continue;

            String type = e.getEventType() == null ? "" : e.getEventType();
            char dir = e.getDirection();

            if ("MOVE".equalsIgnoreCase(type)) {
                maze.move(dir);
            } else if ("AI_MOVE".equalsIgnoreCase(type)) {
                enemyX = e.getEnemyX();
                enemyY = e.getEnemyY();
            } else {
                // 확장 이벤트 타입이 생겨도 좌표/메시지 기반으로 프레임은 구성
                // (필요 시 여기서 추가 시뮬레이션)
            }

            ReplayFrame f = new ReplayFrame();
            f.index = idx++;
            f.offsetMillis = Math.max(0, e.getTimestamp() - baseTs);
            f.eventType = type;
            f.direction = dir;
            f.playerX = e.getPlayerX();
            f.playerY = e.getPlayerY();
            f.enemyX = e.getEnemyX();
            f.enemyY = e.getEnemyY();
            f.message = e.getMessage();
            f.success = e.isSuccess();
            f.mazeView = maze.showMazeNoFog(enemyX, enemyY);
            frames.add(f);
        }

        return frames;
    }

    private static int[][] deepCopy2D(int[][] src) {
        int[][] copy = new int[src.length][];
        for (int i = 0; i < src.length; i++) {
            copy[i] = src[i] == null ? null : src[i].clone();
        }
        return copy;
    }
}

