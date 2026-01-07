package com.example.secuproject;

import java.util.Scanner;

class Maze {
    private int[][] map;
    private int size;
    private int PlayerX;
    private int PlayerY;
    private boolean ItemEffect;
    private long endTime;
    private int viewRange;


    Maze(){
        size = 5; //사이즈 5
        map = new int[][]{
                {0, 3, 4, 3, 9},
                {4, 3, 4, 3, 4},
                {3, 3, 3, 3, 4},
                {3, 4, 4, 6, 3},
                {3, 3, 3, 4, 3}
        };
        findStartSpot();
    }

    private void findStartSpot() {
        for(int i = 0; i < size; i++){
            for(int j = 0; j < size; j++){
                if(map[i][j] == 0){
                    PlayerX = i;
                    PlayerY = j;
                }
            }
        }
    }

    public void showMaze(){
        long now = System.currentTimeMillis();

        if(ItemEffect && now > endTime){
            System.out.println("5초가 지나서 아이템 효과가 꺼집니다.");
            ItemEffect = false;
            viewRange = 1;
        }

        for(int i = 0; i < size; i++){
            for(int j = 0; j < size; j++){

                boolean inView = Math.abs(i - PlayerX) <= viewRange &&
                        Math.abs(j - PlayerY) <= viewRange;

                if(!inView){
                    System.out.print(" ?");
                    continue;
                }

                if(i == PlayerX && j == PlayerY){
                    System.out.print(" P");
                }
                else if(map[i][j] == 4){
                    System.out.print(" #");
                }
                else if(map[i][j] == 9){
                    System.out.print(" G");
                }
                else if(map[i][j] == 6){
                    System.out.println(" *");
                }
                else{
                    System.out.print(" .");
                }
            }
            System.out.println();
        }
    }

    public boolean move(char c){
        int nx = PlayerX;
        int ny = PlayerY;

        if(c == 'w'){nx--;}
        else if(c == 'a'){ny--;}
        else if(c == 's'){nx++;}
        else if(c == 'd'){ny++;}

        if(nx < 0 || size <= nx || ny < 0 || size <= ny){
            System.out.println("범위를 벗어남");
            return false;
        }

        if(map[nx][ny] == 4){
            System.out.println("벽입니다! 진입 불가");
            return false;
        }

        if(map[nx][ny] == 9){
            System.out.println("도착입니다!");
            return true;
        }

        PlayerX = nx;
        PlayerY = ny;

        if(map[nx][ny] == 6){
            System.out.println("아이템 효과 발동!");
            ItemEffect = true;
            endTime = System.currentTimeMillis() * 5000;
            viewRange = 1;
            map[nx][ny] = 1;
        }

        return false;
    }
}

class StartGame {
    private Scanner sc;
    private Maze maze;

    StartGame(){
        maze = new Maze();
        sc = new Scanner(System.in);
    }

    void start(){
        System.out.println("[maze 1.0]");
        System.out.println("미로 게임 시작!");
        while(true){
            maze.showMaze();
            System.out.println("움직이세요 >> [w / a / s / d]");
            char c = sc.next().charAt(0);

            boolean arrive = maze.move(c);

            if(arrive){
                System.out.println("게임이 끝났습니다!");
                break;
            }
        }
    }

}

public class Maze_one {
    public static void main(String[] args) {
        StartGame startGame = new StartGame();
        startGame.start();
    }
}
