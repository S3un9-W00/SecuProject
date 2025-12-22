package com.example.secuproject.controller;

import com.example.secuproject.game.GameEngine;
import com.example.secuproject.generator.MazeGenerator;
import com.example.secuproject.model.Maze;
import com.example.secuproject.model.MazeCell;
import com.example.secuproject.model.Player;
import com.example.secuproject.model.Position;
import com.example.secuproject.util.MazeFileHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class GameController {
    
    @GetMapping("/")
    public String index() {
        return "index";
    }
    
    @PostMapping("/game/start")
    public String startGame(@RequestParam(defaultValue = "10") int size, HttpSession session) {
        MazeGenerator generator = new MazeGenerator();
        Maze maze = generator.generateMaze(size);
        GameEngine gameEngine = new GameEngine(maze);
        session.setAttribute("gameEngine", gameEngine);
        return "redirect:/game";
    }
    
    @PostMapping("/game/load")
    public String loadGame(@RequestParam String filePath, HttpSession session) {
        try {
            Maze maze = MazeFileHandler.loadMaze(filePath);
            GameEngine gameEngine = new GameEngine(maze);
            session.setAttribute("gameEngine", gameEngine);
            return "redirect:/game";
        } catch (Exception e) {
            return "redirect:/?error=" + e.getMessage();
        }
    }
    
    @GetMapping("/game")
    public String game(Model model, HttpSession session) {
        GameEngine gameEngine = (GameEngine) session.getAttribute("gameEngine");
        if (gameEngine == null) {
            return "redirect:/";
        }
        
        model.addAttribute("mazeData", getMazeData(gameEngine));
        model.addAttribute("gameLog", gameEngine.getGameLog());
        model.addAttribute("gameFinished", gameEngine.isGameFinished());
        model.addAttribute("playerMe", getPlayerData(gameEngine.getPlayerMe()));
        model.addAttribute("playerYou", getPlayerData(gameEngine.getPlayerYou()));
        
        return "game";
    }
    
    @PostMapping("/game/move")
    @ResponseBody
    public Map<String, Object> move(@RequestParam String direction, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        GameEngine gameEngine = (GameEngine) session.getAttribute("gameEngine");
        
        if (gameEngine == null) {
            response.put("success", false);
            response.put("message", "게임이 시작되지 않았습니다.");
            return response;
        }
        
        try {
            GameEngine.Direction dir = GameEngine.Direction.valueOf(direction.toUpperCase());
            boolean moved = gameEngine.movePlayer(gameEngine.getPlayerMe(), dir);
            
            response.put("success", true);
            response.put("moved", moved);
            response.put("mazeData", getMazeData(gameEngine));
            response.put("gameLog", gameEngine.getGameLog());
            response.put("gameFinished", gameEngine.isGameFinished());
            response.put("playerMe", getPlayerData(gameEngine.getPlayerMe()));
            response.put("playerYou", getPlayerData(gameEngine.getPlayerYou()));
            
            session.setAttribute("gameEngine", gameEngine);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    @PostMapping("/game/ai-move")
    @ResponseBody
    public Map<String, Object> aiMove(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        GameEngine gameEngine = (GameEngine) session.getAttribute("gameEngine");
        
        if (gameEngine == null) {
            response.put("success", false);
            response.put("message", "게임이 시작되지 않았습니다.");
            return response;
        }
        
        try {
            // AI 이동
            if (!gameEngine.getPlayerYou().isReachedGoal()) {
                com.example.secuproject.ai.AIRobot aiRobot = new com.example.secuproject.ai.AIRobot(gameEngine);
                GameEngine.Direction aiDirection = aiRobot.decideNextMove();
                if (aiDirection != null) {
                    gameEngine.movePlayer(gameEngine.getPlayerYou(), aiDirection);
                }
            }
            
            response.put("success", true);
            response.put("mazeData", getMazeData(gameEngine));
            response.put("gameLog", gameEngine.getGameLog());
            response.put("gameFinished", gameEngine.isGameFinished());
            response.put("playerMe", getPlayerData(gameEngine.getPlayerMe()));
            response.put("playerYou", getPlayerData(gameEngine.getPlayerYou()));
            
            session.setAttribute("gameEngine", gameEngine);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    @PostMapping("/game/reset")
    public String resetGame(HttpSession session) {
        session.removeAttribute("gameEngine");
        return "redirect:/";
    }
    
    private Map<String, Object> getMazeData(GameEngine gameEngine) {
        Map<String, Object> data = new HashMap<>();
        Maze maze = gameEngine.getMaze();
        int size = maze.getSize();
        List<List<Map<String, Object>>> cells = new ArrayList<>();
        
        Player playerMe = gameEngine.getPlayerMe();
        Player playerYou = gameEngine.getPlayerYou();
        
        for (int i = 0; i < size; i++) {
            List<Map<String, Object>> row = new ArrayList<>();
            for (int j = 0; j < size; j++) {
                Map<String, Object> cell = new HashMap<>();
                Position pos = new Position(i, j);
                MazeCell mazeCell = maze.getCell(i, j);
                
                boolean isPlayerMe = pos.equals(playerMe.getPosition());
                boolean isPlayerYou = pos.equals(playerYou.getPosition());
                boolean isVisible = mazeCell.isVisible();
                
                cell.put("code", mazeCell.getCode());
                cell.put("visible", isVisible);
                cell.put("playerMe", isPlayerMe);
                cell.put("playerYou", isPlayerYou);
                
                row.add(cell);
            }
            cells.add(row);
        }
        
        data.put("size", size);
        data.put("cells", cells);
        return data;
    }
    
    private Map<String, Object> getPlayerData(Player player) {
        Map<String, Object> data = new HashMap<>();
        data.put("position", Map.of("x", player.getPosition().getX(), "y", player.getPosition().getY()));
        data.put("hasItem", player.hasItem());
        data.put("reachedGoal", player.isReachedGoal());
        if (player.getFinishTime() > 0) {
            data.put("finishTime", player.getFinishTime() / 1000.0);
        }
        return data;
    }
}

