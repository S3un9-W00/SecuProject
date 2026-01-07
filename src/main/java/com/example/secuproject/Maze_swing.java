package com.example.secuproject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class Maze_swing extends JFrame {

    private Maze_two maze;
    private MazePanel panel;
    private JLabel msgLabel;

    private Enemy enemy;       // 상대 (외부 Enemy 클래스 사용)
    private Timer enemyTimer;  // 자동 이동 타이머
    private boolean gameFinished = false; // 게임 종료 여부

    public Maze_swing() {
        // txt 파일에서 미로 읽기 (Spring과 동일)
        try {
            maze = Maze_two.fromFile("maze.txt");
        } catch (IOException e) {
            System.out.println("파일 읽기 실패: " + e.getMessage() + " - 기본 미로 사용");
            maze = new Maze_two(); // 파일 읽기 실패 시 기본 미로 사용
        }

        setTitle("Maze_two Swing with Enemy");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        panel = new MazePanel();
        msgLabel = new JLabel("WASD로 이동, Enemy는 오른손 법칙으로 자동 이동.");

        add(panel, BorderLayout.CENTER);
        add(msgLabel, BorderLayout.SOUTH);

        // Enemy 초기 위치 찾기 (플레이어와 다른 스타트 지점)
        int[][] map = maze.getMap();
        int enemyStartX = findEnemyStartX();
        int enemyStartY = findEnemyStartY();
        enemy = new Enemy(maze, enemyStartX, enemyStartY, map);

        // 플레이어 키 입력
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (gameFinished) {
                    return; // 게임 종료 시 입력 무시
                }
                
                char c = Character.toLowerCase(e.getKeyChar());
                if (c == 'w' || c == 'a' || c == 's' || c == 'd') {
                    Maze_two.MoveResult result = maze.move(c);
                    msgLabel.setText(result.message);
                    
                    // 도착지점 도달 확인
                    if (result.arrived) {
                        gameFinished = true;
                        enemyTimer.stop();
                        msgLabel.setText("축하합니다! 도착지점에 도달했습니다!");
                        showGameOverDialog("플레이어 승리!");
                    }
                    
                    panel.repaint();
                }
            }
        });

        // 적 자동 이동: 0.5초마다 한 칸 (Spring과 동일)
        enemyTimer = new Timer(500, e -> {
            if (gameFinished) {
                enemyTimer.stop();
                return;
            }
            
            enemy.step();
            
            // Enemy 도착지점 도달 확인
            if (map[enemy.getX()][enemy.getY()] == 9) {
                gameFinished = true;
                enemyTimer.stop();
                msgLabel.setText("Enemy가 먼저 도착했습니다!");
                showGameOverDialog("Enemy 승리!");
            }
            
            panel.repaint();
        });
        enemyTimer.start();

        setSize(400, 450);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }

    // 미로 + 플레이어 + 적 그리기
    private class MazePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Enemy 위치를 포함하여 미로 표시 (Spring과 동일)
            String mazeView = maze.showMaze(enemy.getX(), enemy.getY());
            String[] lines = mazeView.split("\n");
            int size = lines.length;
            int cellSize = Math.min(getWidth(), getHeight()) / size;

            for (int i = 0; i < size; i++) {
                String[] tokens = lines[i].trim().split("\\s+");
                for (int j = 0; j < tokens.length; j++) {
                    String t = tokens[j];

                    int x = j * cellSize;
                    int y = i * cellSize;

                    g.setColor(Color.BLACK);
                    g.fillRect(x, y, cellSize, cellSize);

                    // 기본 바탕
                    if (t.equals("?")) {
                        g.setColor(Color.DARK_GRAY);
                    } else if (t.equals("#")) {
                        g.setColor(Color.GRAY);
                    } else if (t.equals(".")) {
                        g.setColor(Color.WHITE);
                    } else if (t.equals("G")) {
                        g.setColor(Color.YELLOW);
                    } else if (t.equals("P")) {
                        g.setColor(Color.GREEN);
                    } else if (t.equals("E")) {
                        g.setColor(Color.RED); // Enemy
                    } else if (t.equals("*")) {
                        g.setColor(Color.CYAN);
                    } else {
                        g.setColor(Color.BLACK);
                    }
                    g.fillRect(x + 2, y + 2, cellSize - 4, cellSize - 4);
                }
            }
        }
    }
    
    /**
     * Enemy를 위한 시작 위치 찾기 (플레이어와 다른 스타트 지점)
     */
    private int findEnemyStartX() {
        int playerX = maze.getPlayerX();
        int playerY = maze.getPlayerY();
        int[][] map = maze.getMap();
        int size = maze.getSize();
        
        // 플레이어와 다른 스타트 지점 찾기
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (map[i][j] == 0 && (i != playerX || j != playerY)) {
                    return i;
                }
            }
        }
        
        // 스타트 지점이 하나면 플레이어 근처의 이동 가능한 위치 찾기
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (map[i][j] != 4 && map[i][j] != 9 && (i != playerX || j != playerY)) {
                    return i;
                }
            }
        }
        
        // 기본값: 플레이어 위치에서 1칸 떨어진 곳
        return Math.min(playerX + 1, size - 1);
    }

    private int findEnemyStartY() {
        int playerX = maze.getPlayerX();
        int playerY = maze.getPlayerY();
        int[][] map = maze.getMap();
        int size = maze.getSize();
        
        // 플레이어와 다른 스타트 지점 찾기
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (map[i][j] == 0 && (i != playerX || j != playerY)) {
                    return j;
                }
            }
        }
        
        // 스타트 지점이 하나면 플레이어 근처의 이동 가능한 위치 찾기
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (map[i][j] != 4 && map[i][j] != 9 && (i != playerX || j != playerY)) {
                    return j;
                }
            }
        }
        
        // 기본값: 플레이어 위치에서 1칸 떨어진 곳
        return Math.min(playerY + 1, size - 1);
    }
    
    /**
     * 게임 종료 다이얼로그 표시
     */
    private void showGameOverDialog(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "게임 종료",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Maze_swing::new);
    }
}
