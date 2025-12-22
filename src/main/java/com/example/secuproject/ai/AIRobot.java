package com.example.secuproject.ai;

import com.example.secuproject.game.GameEngine;
import com.example.secuproject.model.Maze;
import com.example.secuproject.model.MazeCell;
import com.example.secuproject.model.Player;
import com.example.secuproject.model.Position;

import java.util.*;

/**
 * AI 로봇 클래스
 * '너' 플레이어를 자동으로 조작하여 도착지점까지 이동합니다
 */
public class AIRobot {
    private GameEngine gameEngine;
    
    public AIRobot(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }
    
    /**
     * AI가 다음 이동을 결정합니다
     * A* 알고리즘을 사용하여 최적 경로를 찾습니다
     * @return 이동할 방향 (null이면 이동 불가)
     */
    public GameEngine.Direction decideNextMove() {
        Player aiPlayer = gameEngine.getPlayerYou();
        Position current = aiPlayer.getPosition();
        Position goal = gameEngine.getMaze().getGoalPosition();
        
        if (goal == null) {
            return null;
        }
        
        // A* 알고리즘으로 최적 경로 찾기
        List<GameEngine.Direction> path = findPath(current, goal);
        
        if (path != null && !path.isEmpty()) {
            return path.get(0); // 다음 이동 방향 반환
        }
        
        // 경로를 찾지 못하면 랜덤하게 이동 시도
        return findRandomMove(aiPlayer);
    }
    
    /**
     * A* 알고리즘을 사용하여 경로를 찾습니다
     */
    private List<GameEngine.Direction> findPath(Position start, Position goal) {
        // A* 알고리즘 구현
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingInt(n -> n.fCost));
        Set<String> closedSet = new HashSet<>();
        Map<String, Node> allNodes = new HashMap<>();
        
        Node startNode = new Node(start, null, 0, heuristic(start, goal));
        openSet.add(startNode);
        allNodes.put(start.toString(), startNode);
        
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        GameEngine.Direction[] directions = {
            GameEngine.Direction.UP,
            GameEngine.Direction.DOWN,
            GameEngine.Direction.LEFT,
            GameEngine.Direction.RIGHT
        };
        
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            closedSet.add(current.position.toString());
            
            if (current.position.equals(goal)) {
                // 경로 재구성
                return reconstructPath(current);
            }
            
            for (int i = 0; i < 4; i++) {
                int nx = current.position.getX() + dx[i];
                int ny = current.position.getY() + dy[i];
                Position neighbor = new Position(nx, ny);
                
                if (closedSet.contains(neighbor.toString())) {
                    continue;
                }
                
                if (!gameEngine.getMaze().isValidPosition(neighbor) ||
                    !gameEngine.getMaze().isWalkable(neighbor)) {
                    continue;
                }
                
                // 다른 플레이어 위치 확인
                if (neighbor.equals(gameEngine.getPlayerMe().getPosition())) {
                    continue;
                }
                
                int tentativeG = current.gCost + 1;
                String key = neighbor.toString();
                
                Node neighborNode = allNodes.get(key);
                if (neighborNode == null) {
                    neighborNode = new Node(neighbor, current, tentativeG, heuristic(neighbor, goal));
                    allNodes.put(key, neighborNode);
                    openSet.add(neighborNode);
                } else if (tentativeG < neighborNode.gCost) {
                    neighborNode.gCost = tentativeG;
                    neighborNode.fCost = tentativeG + neighborNode.hCost;
                    neighborNode.parent = current;
                    openSet.remove(neighborNode);
                    openSet.add(neighborNode);
                }
            }
        }
        
        return null; // 경로를 찾지 못함
    }
    
    /**
     * 휴리스틱 함수 (맨해튼 거리)
     */
    private int heuristic(Position a, Position b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }
    
    /**
     * 경로를 재구성합니다
     */
    private List<GameEngine.Direction> reconstructPath(Node goal) {
        List<GameEngine.Direction> path = new ArrayList<>();
        Node current = goal;
        
        while (current.parent != null) {
            Position currentPos = current.position;
            Position parentPos = current.parent.position;
            
            int dx = currentPos.getX() - parentPos.getX();
            int dy = currentPos.getY() - parentPos.getY();
            
            if (dx == -1) {
                path.add(0, GameEngine.Direction.UP);
            } else if (dx == 1) {
                path.add(0, GameEngine.Direction.DOWN);
            } else if (dy == -1) {
                path.add(0, GameEngine.Direction.LEFT);
            } else if (dy == 1) {
                path.add(0, GameEngine.Direction.RIGHT);
            }
            
            current = current.parent;
        }
        
        return path;
    }
    
    /**
     * 랜덤하게 이동 가능한 방향을 찾습니다
     */
    private GameEngine.Direction findRandomMove(Player player) {
        List<GameEngine.Direction> possibleMoves = new ArrayList<>();
        Position current = player.getPosition();
        
        GameEngine.Direction[] directions = {
            GameEngine.Direction.UP,
            GameEngine.Direction.DOWN,
            GameEngine.Direction.LEFT,
            GameEngine.Direction.RIGHT
        };
        
        for (GameEngine.Direction dir : directions) {
            int x = current.getX();
            int y = current.getY();
            
            switch (dir) {
                case UP: x--; break;
                case DOWN: x++; break;
                case LEFT: y--; break;
                case RIGHT: y++; break;
            }
            
            Position newPos = new Position(x, y);
            if (gameEngine.getMaze().isWalkable(newPos) &&
                !newPos.equals(gameEngine.getPlayerMe().getPosition())) {
                possibleMoves.add(dir);
            }
        }
        
        if (possibleMoves.isEmpty()) {
            return null;
        }
        
        Random random = new Random();
        return possibleMoves.get(random.nextInt(possibleMoves.size()));
    }
    
    /**
     * A* 알고리즘을 위한 노드 클래스
     */
    private static class Node {
        Position position;
        Node parent;
        int gCost; // 시작점부터의 비용
        int hCost; // 목표점까지의 휴리스틱 비용
        int fCost; // gCost + hCost
        
        Node(Position position, Node parent, int gCost, int hCost) {
            this.position = position;
            this.parent = parent;
            this.gCost = gCost;
            this.hCost = hCost;
            this.fCost = gCost + hCost;
        }
    }
}

