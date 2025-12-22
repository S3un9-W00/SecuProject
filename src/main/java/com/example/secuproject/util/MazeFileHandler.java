package com.example.secuproject.util;

import com.example.secuproject.model.Maze;
import com.example.secuproject.model.MazeCell;
import com.example.secuproject.model.Position;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 미로 파일을 읽고 쓰는 클래스
 * 텍스트 파일 형식으로 미로를 저장하고 불러옵니다
 */
public class MazeFileHandler {
    
    /**
     * 텍스트 파일에서 미로를 읽어옵니다
     * @param filePath 파일 경로
     * @return 읽어온 미로 객체
     */
    public static Maze loadMaze(String filePath) throws IOException {
        List<String> lines = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line.trim());
            }
        }
        
        if (lines.isEmpty()) {
            throw new IOException("파일이 비어있습니다");
        }
        
        int size = lines.size();
        Maze maze = new Maze(size);
        
        // 각 줄을 읽어서 미로를 구성합니다
        for (int i = 0; i < size; i++) {
            String line = lines.get(i);
            // 공백이나 구분자 없이 숫자만 있는 경우와 공백으로 구분된 경우 모두 처리
            String[] parts = line.split("\\s+");
            
            if (parts.length == 1 && parts[0].length() == size) {
                // 한 줄에 숫자가 붙어있는 경우 (예: "4444444")
                String numbers = parts[0];
                for (int j = 0; j < size && j < numbers.length(); j++) {
                    int code = Character.getNumericValue(numbers.charAt(j));
                    maze.setCell(i, j, code);
                }
            } else {
                // 공백으로 구분된 경우
                for (int j = 0; j < parts.length && j < size; j++) {
                    try {
                        int code = Integer.parseInt(parts[j]);
                        maze.setCell(i, j, code);
                    } catch (NumberFormatException e) {
                        // 숫자가 아니면 벽(4)으로 처리
                        maze.setCell(i, j, 4);
                    }
                }
            }
        }
        
        return maze;
    }
    
    /**
     * 미로를 텍스트 파일로 저장합니다
     * @param maze 저장할 미로
     * @param filePath 저장할 파일 경로
     */
    public static void saveMaze(Maze maze, String filePath) throws IOException {
        // 파일 경로에서 디렉토리 부분 추출
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        
        // 부모 디렉토리가 있고 존재하지 않으면 생성
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            int size = maze.getSize();
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    MazeCell cell = maze.getCell(i, j);
                    writer.print(cell.getCode());
                    if (j < size - 1) {
                        writer.print(" ");
                    }
                }
                writer.println();
            }
        }
    }
    
    /**
     * 미로를 콘솔에 출력합니다 (디버깅용)
     */
    public static void printMaze(Maze maze) {
        int size = maze.getSize();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                MazeCell cell = maze.getCell(i, j);
                System.out.print(cell.getCode() + " ");
            }
            System.out.println();
        }
    }
}

