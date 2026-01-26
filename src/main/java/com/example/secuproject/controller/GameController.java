package com.example.secuproject.controller;

import com.example.secuproject.Maze_two;
import com.example.secuproject.Service.MazeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 간단한 미로 게임 컨트롤러
 * Maze_two와 Enemy만 사용하여 게임을 관리합니다
 */
@Controller
public class GameController {
    
    @Autowired
    private MazeService mazeService;
    
    /**
     * 메인 페이지
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }
    
    /**
     * 게임 시작
     */
    @PostMapping("/game/start")
    public String startGame() {
        mazeService.startGame();
        return "redirect:/game";
    }
    
    /**
     * 게임 화면
     */
    @GetMapping("/game")
    public String game(Model model) {
        MazeService.GameStatus status = mazeService.getStatus();
        model.addAttribute("status", status);
        return "game";
    }
    
    /**
     * 플레이어 이동 (w/a/s/d)
     * AI는 자동으로 움직이지 않음 (2초마다 별도로 움직임)
     */
    @PostMapping("/game/move")
    @ResponseBody
    public Map<String, Object> move(@RequestParam String direction) {
        Map<String, Object> response = new HashMap<>();
        
        if (direction == null || direction.isEmpty()) {
            response.put("success", false);
            response.put("message", "방향을 입력해주세요.");
            return response;
        }
        
        char dir = direction.toLowerCase().charAt(0);
        Maze_two.MoveResult result = mazeService.move(dir);
        
        // AI는 자동으로 움직이지 않음 (2초마다 별도로 움직임)
        
        MazeService.GameStatus status = mazeService.getStatus();
        
        response.put("success", result.moved);
        response.put("moved", result.moved);
        response.put("arrived", result.arrived);
        response.put("message", result.message);
        response.put("mazeView", status.mazeView);
        response.put("playerX", status.playerX);
        response.put("playerY", status.playerY);
        response.put("enemyX", status.enemyX);
        response.put("enemyY", status.enemyY);
        response.put("gameFinished", status.gameFinished);
        
        return response;
    }
    
    /**
     * AI만 이동 (테스트용)
     */
    @PostMapping("/game/ai-move")
    @ResponseBody
    public Map<String, Object> aiMove() {
        Map<String, Object> response = new HashMap<>();
        
        mazeService.aiMove();
        
        MazeService.GameStatus status = mazeService.getStatus();
        
        response.put("success", true);
        response.put("mazeView", status.mazeView);
        response.put("enemyX", status.enemyX);
        response.put("enemyY", status.enemyY);
        response.put("gameFinished", status.gameFinished);
        
        return response;
    }
    
    /**
     * 게임 리셋 - 로그 저장 후 메인 페이지로
     */
    @PostMapping("/game/reset")
    public String resetGame() {
        String logFile = mazeService.reset();
        if (logFile != null) {
            System.out.println("🎮 게임 로그 저장됨: " + logFile);
        }
        return "redirect:/";
    }
    
    /**
     * 게임 상태 조회 (AJAX용)
     */
    @GetMapping("/game/status")
    @ResponseBody
    public Map<String, Object> getStatus() {
        Map<String, Object> response = new HashMap<>();
        MazeService.GameStatus status = mazeService.getStatus();
        
        response.put("mazeView", status.mazeView);
        response.put("playerX", status.playerX);
        response.put("playerY", status.playerY);
        response.put("enemyX", status.enemyX);
        response.put("enemyY", status.enemyY);
        response.put("gameStarted", status.gameStarted);
        response.put("gameFinished", status.gameFinished);
        
        return response;
    }

    /**
     * 게임 로그 저장 (선택사항 - 게임 종료 전에 수동 저장)
     */
    @PostMapping("/game/save-log")
    @ResponseBody
    public Map<String, Object> saveLog() {
        Map<String, Object> response = new HashMap<>();
        String logFile = mazeService.saveGameLog();
        
        if (logFile != null) {
            response.put("success", true);
            response.put("logFile", logFile);
            response.put("message", "게임 로그가 저장되었습니다.");
        } else {
            response.put("success", false);
            response.put("message", "로그 저장에 실패했습니다.");
        }
        
        return response;
    }
}
