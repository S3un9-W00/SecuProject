package com.example.secuproject.ui;

import com.example.secuproject.game.GameEngine;
import com.example.secuproject.model.Maze;
import com.example.secuproject.model.MazeCell;
import com.example.secuproject.model.Player;
import com.example.secuproject.model.Position;

import java.util.Scanner;

/**
 * 게임 화면을 표시하고 사용자 입력을 처리하는 클래스
 */
public class GameUI {
    private GameEngine gameEngine;
    private Scanner scanner;
    
    public GameUI(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
        this.scanner = new Scanner(System.in);
    }
    
    /**
     * 게임 화면을 출력합니다
     */
    public void displayGame() {
        clearScreen();
        
        Maze maze = gameEngine.getMaze();
        Player playerMe = gameEngine.getPlayerMe();
        Player playerYou = gameEngine.getPlayerYou();
        
        System.out.println("========================================");
        System.out.println("         미로찾기 게임");
        System.out.println("========================================");
        System.out.println();
        
        // 미로 출력
        int size = maze.getSize();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Position pos = new Position(i, j);
                MazeCell cell = maze.getCell(i, j);
                
                // 플레이어 위치 확인
                if (pos.equals(playerMe.getPosition())) {
                    System.out.print("※ "); // 나 플레이어
                } else if (pos.equals(playerYou.getPosition())) {
                    System.out.print("○ "); // 너 플레이어
                } else if (!cell.isVisible()) {
                    // 안개로 가려진 칸
                    System.out.print("□ "); // 안개
                } else {
                    // 가시화된 칸
                    int code = cell.getCode();
                    switch (code) {
                        case 0:
                            System.out.print("S "); // 스타트
                            break;
                        case 3:
                            System.out.print("· "); // 길
                            break;
                        case 4:
                            System.out.print("■ "); // 벽
                            break;
                        case 6:
                            System.out.print("★ "); // 아이템
                            break;
                        case 9:
                            System.out.print("G "); // 도착지점
                            break;
                        default:
                            System.out.print("□ "); // 기타 (안개)
                            break;
                    }
                }
            }
            System.out.println();
        }
        
        System.out.println();
        System.out.println("========================================");
        System.out.println("범례: ※ = 나, ○ = 너, S = 스타트, G = 도착, ★ = 아이템, ■ = 벽, · = 길, □ = 안개");
        System.out.println("========================================");
        System.out.println();
        
        // 플레이어 상태 정보
        System.out.println("나의 상태:");
        System.out.println("  위치: " + playerMe.getPosition());
        System.out.println("  아이템: " + (playerMe.hasItem() ? "활성화 (3×3 스캔)" : "없음"));
        System.out.println("  도착 여부: " + (playerMe.isReachedGoal() ? "도착함 ✓" : "진행 중"));
        
        System.out.println();
        System.out.println("너의 상태:");
        System.out.println("  위치: " + playerYou.getPosition());
        System.out.println("  아이템: " + (playerYou.hasItem() ? "활성화 (3×3 스캔)" : "없음"));
        System.out.println("  도착 여부: " + (playerYou.isReachedGoal() ? "도착함 ✓" : "진행 중"));
        
        System.out.println();
        System.out.println("========================================");
        System.out.println("조작 방법: W(위), S(아래), A(왼쪽), D(오른쪽), Q(종료)");
        System.out.println("========================================");
    }
    
    /**
     * 게임 로그를 출력합니다
     */
    public void displayLog() {
        System.out.println();
        System.out.println("========================================");
        System.out.println("게임 로그:");
        System.out.println("========================================");
        var logs = gameEngine.getGameLog();
        int startIndex = Math.max(0, logs.size() - 10); // 최근 10개만 표시
        for (int i = startIndex; i < logs.size(); i++) {
            System.out.println(logs.get(i));
        }
        System.out.println("========================================");
    }
    
    /**
     * 사용자 입력을 받아 이동 방향을 반환합니다
     */
    public GameEngine.Direction getPlayerInput() {
        System.out.print("이동할 방향을 입력하세요 (W/S/A/D): ");
        String input = scanner.nextLine().trim().toUpperCase();
        
        switch (input) {
            case "W":
                return GameEngine.Direction.UP;
            case "S":
                return GameEngine.Direction.DOWN;
            case "A":
                return GameEngine.Direction.LEFT;
            case "D":
                return GameEngine.Direction.RIGHT;
            case "Q":
                return null; // 종료
            default:
                System.out.println("잘못된 입력입니다. W, S, A, D 중 하나를 입력하세요.");
                return getPlayerInput(); // 다시 입력 받기
        }
    }
    
    /**
     * 게임 종료 화면을 출력합니다
     */
    public void displayGameOver() {
        clearScreen();
        System.out.println("========================================");
        System.out.println("         게임 종료!");
        System.out.println("========================================");
        System.out.println();
        
        Player playerMe = gameEngine.getPlayerMe();
        Player playerYou = gameEngine.getPlayerYou();
        
        if (playerMe.isReachedGoal() && playerYou.isReachedGoal()) {
            long timeMe = playerMe.getFinishTime();
            long timeYou = playerYou.getFinishTime();
            
            System.out.println("양쪽 모두 도착했습니다!");
            System.out.println("나의 완료 시간: " + (timeMe / 1000.0) + "초");
            System.out.println("너의 완료 시간: " + (timeYou / 1000.0) + "초");
            
            if (timeMe < timeYou) {
                System.out.println("승자: 나!");
            } else if (timeYou < timeMe) {
                System.out.println("승자: 너!");
            } else {
                System.out.println("무승부!");
            }
        } else if (playerMe.isReachedGoal()) {
            System.out.println("나가 먼저 도착했습니다!");
        } else if (playerYou.isReachedGoal()) {
            System.out.println("너가 먼저 도착했습니다!");
        }
        
        System.out.println();
        displayLog();
    }
    
    /**
     * 화면을 지웁니다 (콘솔 클리어)
     */
    private void clearScreen() {
        // Windows와 Unix 모두 지원
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // 클리어 실패 시 여러 줄 출력으로 대체
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }
    
    /**
     * Scanner를 닫습니다
     */
    public void close() {
        scanner.close();
    }
}

