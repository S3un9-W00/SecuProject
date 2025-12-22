package com.example.secuproject;

import com.example.secuproject.ai.AIRobot;
import com.example.secuproject.game.GameEngine;
import com.example.secuproject.generator.MazeGenerator;
import com.example.secuproject.model.Maze;
import com.example.secuproject.ui.GameUI;
import com.example.secuproject.util.MazeFileHandler;

import java.util.Scanner;

/**
 * 미로찾기 게임의 메인 클래스
 * 게임 실행 및 메뉴를 관리합니다
 */
public class MazeGame {
    private static Scanner scanner = new Scanner(System.in);
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("     미로찾기 게임에 오신 것을 환영합니다!");
        System.out.println("========================================");
        System.out.println();
        
        while (true) {
            showMainMenu();
            int choice = getMenuChoice();
            
            switch (choice) {
                case 1:
                    playGameWithNewMaze();
                    break;
                case 2:
                    playGameWithFile();
                    break;
                case 3:
                    generateMazeFile();
                    break;
                case 4:
                    System.out.println("게임을 종료합니다. 감사합니다!");
                    scanner.close();
                    return;
                default:
                    System.out.println("잘못된 선택입니다. 다시 선택해주세요.");
                    break;
            }
        }
    }
    
    /**
     * 메인 메뉴를 표시합니다
     */
    private static void showMainMenu() {
        System.out.println();
        System.out.println("========================================");
        System.out.println("            메인 메뉴");
        System.out.println("========================================");
        System.out.println("1. 새 미로로 게임 시작");
        System.out.println("2. 파일에서 미로 불러오기");
        System.out.println("3. 미로 파일 생성");
        System.out.println("4. 종료");
        System.out.println("========================================");
        System.out.print("선택하세요 (1-4): ");
    }
    
    /**
     * 메뉴 선택을 받습니다
     */
    private static int getMenuChoice() {
        try {
            String input = scanner.nextLine().trim();
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    /**
     * 새 미로를 생성하여 게임을 시작합니다
     */
    private static void playGameWithNewMaze() {
        System.out.println();
        System.out.print("미로 크기를 입력하세요 (5 이상 권장): ");
        int size;
        try {
            size = Integer.parseInt(scanner.nextLine().trim());
            if (size < 5) {
                System.out.println("크기가 너무 작습니다. 최소 5로 설정합니다.");
                size = 5;
            }
        } catch (NumberFormatException e) {
            System.out.println("잘못된 입력입니다. 기본 크기 10을 사용합니다.");
            size = 10;
        }
        
        System.out.println("미로를 생성하는 중...");
        MazeGenerator generator = new MazeGenerator();
        Maze maze = generator.generateMaze(size);
        
        System.out.println("미로 생성 완료! 게임을 시작합니다.");
        System.out.println("아무 키나 누르면 게임이 시작됩니다...");
        scanner.nextLine();
        
        startGame(maze);
    }
    
    /**
     * 파일에서 미로를 불러와 게임을 시작합니다
     */
    private static void playGameWithFile() {
        System.out.println();
        System.out.print("미로 파일 경로를 입력하세요: ");
        String filePath = scanner.nextLine().trim();
        
        try {
            Maze maze = MazeFileHandler.loadMaze(filePath);
            System.out.println("미로를 불러왔습니다! 게임을 시작합니다.");
            System.out.println("아무 키나 누르면 게임이 시작됩니다...");
            scanner.nextLine();
            
            startGame(maze);
        } catch (Exception e) {
            System.out.println("파일을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 미로 파일을 생성합니다
     */
    private static void generateMazeFile() {
        System.out.println();
        System.out.print("미로 크기를 입력하세요 (5 이상 권장): ");
        int size;
        try {
            size = Integer.parseInt(scanner.nextLine().trim());
            if (size < 5) {
                System.out.println("크기가 너무 작습니다. 최소 5로 설정합니다.");
                size = 5;
            }
        } catch (NumberFormatException e) {
            System.out.println("잘못된 입력입니다. 기본 크기 10을 사용합니다.");
            size = 10;
        }
        
        System.out.print("저장할 파일 경로를 입력하세요: ");
        String filePath = scanner.nextLine().trim();
        
        try {
            System.out.println("미로를 생성하는 중...");
            MazeGenerator generator = new MazeGenerator();
            Maze maze = generator.generateMaze(size);
            
            MazeFileHandler.saveMaze(maze, filePath);
            System.out.println("미로 파일이 생성되었습니다: " + filePath);
        } catch (Exception e) {
            System.out.println("파일을 저장하는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 게임을 시작합니다
     */
    private static void startGame(Maze maze) {
        GameEngine gameEngine = new GameEngine(maze);
        GameUI gameUI = new GameUI(gameEngine);
        AIRobot aiRobot = new AIRobot(gameEngine);
        
        // 게임 루프
        while (!gameEngine.isGameFinished()) {
            // 화면 표시
            gameUI.displayGame();
            gameUI.displayLog();
            
            // 플레이어 입력 받기
            GameEngine.Direction playerDirection = gameUI.getPlayerInput();
            
            if (playerDirection == null) {
                // Q 입력 시 게임 종료
                System.out.println("게임을 종료합니다.");
                break;
            }
            
            // 플레이어 이동
            boolean moved = gameEngine.movePlayer(gameEngine.getPlayerMe(), playerDirection);
            if (!moved) {
                System.out.println("이동할 수 없는 위치입니다!");
                try {
                    Thread.sleep(1000); // 1초 대기
                } catch (InterruptedException e) {
                    // 무시
                }
            }
            
            // AI 이동 (플레이어가 도착하지 않았을 때만)
            if (!gameEngine.getPlayerYou().isReachedGoal()) {
                GameEngine.Direction aiDirection = aiRobot.decideNextMove();
                if (aiDirection != null) {
                    gameEngine.movePlayer(gameEngine.getPlayerYou(), aiDirection);
                }
            }
            
            // 잠시 대기 (화면이 너무 빠르게 바뀌는 것을 방지)
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                // 무시
            }
        }
        
        // 게임 종료 화면
        gameUI.displayGameOver();
        System.out.println();
        System.out.println("아무 키나 누르면 메인 메뉴로 돌아갑니다...");
        scanner.nextLine();
    }
}

