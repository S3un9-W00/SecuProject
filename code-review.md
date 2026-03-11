# 코드 리뷰 보고서

**프로젝트:** SecuProject (미로 찾기 게임)  
**작성일:** 2026-03-11  
**전체 파일 수:** 24개 Java 파일

---

## 1. 아키텍처 문제

### 1.1 중복 미로 클래스
두 개의 미로 클래스가 존재하여 유지보수가 어려움:

| 클래스 | 위치 | 사용처 |
|--------|------|--------|
| `Maze_two` | 루트 패키지 | MazeService에서 실제 사용 |
| `model/Maze` | model 패키지 | GameEngine에서 사용 (미사용) |

**문제:** `GameEngine`이 실제로 MazeService에서 사용되지 않음. 사용하지 않는 코드가 프로젝트에 남아있음.

### 1.2 중복 제네레이터
두 버전의 미로 생성기가 존재:

- `generator/MazeGenerator.java` - Prim's 알고리즘, Spring용
- `util/MazeGenerator.java` - standalone 실행용

**권장:** 하나의 클래스로 통합하거나, 명확한 역할 분담 필요.

---

## 2. 코드 중복

### 2.1 Enemy 시작 위치 찾기 로직
`MazeService.java`의 `findEnemyStartX()`와 `findEnemyStartY()`가 동일한 로직을 중복 구현:

```java
// 두 메서드가 거의 동일한 코드
private int findEnemyStartX() { ... }
private int findEnemyStartY() { ... }
```

**권장:** 하나로 통합하여 `findEnemyStart()` 반환.

### 2.2 시야 범위 계산 로직 중복
- `Maze_two.showMaze()`
- `model/Maze.updateVisibility()`
- `Enemy.updateVision()`

세 곳에서 유사한 시야 계산 로직이 중복됨.

---

## 3. 게임 로직 문제

### 3.1 게임 종료 조건 불명확
`MazeService.java`에서:
- `playerArrived`와 `enemyArrived`가 사용되지만 정확한 상태 전이 규칙이不明显
- 플레이어와 Enemy가 동시에 도착지점에 도달한 경우 처리 로직 없음

### 3.2 로그 중복 저장
여러 곳에서 `gameLogger.finishAndSave()` 호출:
- `move()`에서 게임 종료 시
- `aiMove()`에서 게임 종료 시
- `reset()`에서도 호출

**문제:** 게임 한 번에 로그가 여러 번 저장될 가능성.

---

## 4. 보안 및 예외 처리

### 4.1 파일 경로 검증 부족
`MazeService.loadReplayFrames()`에서:
```java
public List<ReplayFrame> loadReplayFrames(String logFilePath) {
    // logFilePath에 대한 검증 없음
    GameLog log = gameLogger.loadLog(logFilePath);
}
```

**문제:** 경로 조작 (path traversal) 공격에 취약할 수 있음.

### 4.2 널PointerException 위험
여러 곳에서 널 체크 없이 객체 접근:
```java
// MazeService.java:150
if (enemy != null) {
    return maze.showMaze(enemy.getX(), enemy.getY());
}
```
이런 체크가 일부만 되어 있음.

---

## 5. 일관성 문제

### 5.1 아이템 코드 불명확
코드상 아이템이 6, 7, 8로 표시되지만:
- `Maze_two.java`: 횃불=6, 망치=7, 함정=8 주석 있음
- `MazeCell.java`: 주석에 6=아이템으로만 표시
- 실제 게임에서 다른 의미로 사용될 수 있음

### 5.2 naming 불일치
- `mazeService` (camelCase)
- `gameLogger` (camelCase)
- `DX`, `DY` (대문자 상수)

---

## 6. 미사용 코드

### 6.1 개발 단계 파일이 그대로 존재
| 파일 | 설명 |
|------|------|
| `Maze_one.java` | 1단계: 콘솔 게임 |
| `Maze_swing.java` | 3단계: Swing GUI |
| `Swing_test.java` | 테스트용 |

이 파일들을 계속 유지할지 결정 필요.

---

## 7. 개선 권장사항

### 높음 (High Priority)
1. **로그 중복 저장 수정** - 한 게임에 하나만 저장되도록 변경
2. **파일 경로 검증 추가** - path traversal 방지
3. **중복 클래스 정리** - Maze_two와 model/Maze 중 하나 선택

### 중간 (Medium Priority)
4. **Enemy 시작 위치 찾기 메서드 통합**
5. **시야 범위 로직 공통화**
6. **게임 종료 상태 머신 명확화**
7. **로그 파일 텍스트 포맷 추가** - 직렬화之外에도 읽기 쉬운 텍스트 로그 지원

### 낮음 (Low Priority)
7. **미사용 파일 정리 atau 주석 추가**
8. **naming 컨벤션 통일**
9. **单元测试 추가**

---

## 8. 듀얼 플레이어 골인 처리 분석

### 8.1 현재 구현 방식

현재 코드에서는 플레이어 또는 Enemy 중 **하나라도** 도착지점에 도달하면 즉시 게임이 종료됩니다.

**MazeService.java (라인 185-194):**
```java
// 플레이어 도착 시
if (result.arrived && !logSaved) {
    playerArrived = true;
    gameFinished = true;  // 즉시 게임 종료!
    logSaved = true;
    String logFile = gameLogger.finishAndSave(playerArrived, enemyArrived);
    // ...
}
```

**MazeService.java (라인 218-233):**
```java
// Enemy 도착 시
if (map[ex][ey] == 9 && !logSaved) {
    enemyArrived = true;
    gameFinished = true;  // 즉시 게임 종료!
    logSaved = true;
    String logFile = gameLogger.finishAndSave(playerArrived, enemyArrived);
    // ...
}
```

**Maze_swing.java (라인 57-63, 79-85):**
```java
// 플레이어 도착
if (result.arrived) {
    gameFinished = true;
    enemyTimer.stop();
    showGameOverDialog("플레이어 승리!");  // 즉시 종료
}

// Enemy 도착
if (map[enemy.getX()][enemy.getY()] == 9) {
    gameFinished = true;
    enemyTimer.stop();
    showGameOverDialog("Enemy 승리!");  // 즉시 종료
}
```

| 단계 | 현재 동작 | 원하는 동작 |
|------|----------|------------|
| 1번째 골인 | 게임 즉시 종료 | "X 통과" 메시지 표시, 게임 계속 |
| 2번째 골인 | (게임 уже 종료됨) | 게임 종료, 결과 표시 |

### 8.3 구현 방법

**수정 필요한 부분:**

1. **MazeService.java** - `gameFinished` 로직 변경
   ```java
   // 현재 (즉시 종료)
   if (result.arrived && !logSaved) {
       playerArrived = true;
       gameFinished = true;  // 문제: 여기서 바로 종료
       // ...
   }
   
   // 수정 후 (계속 진행)
   if (result.arrived && !playerArrived) {
       playerArrived = true;
       // gameFinished는 두 번째 도착할 때까지 false 유지
       // "플레이어 통과" 메시지 반환
   }
   
   // 두 번째 도착 시 (누구든)
   if ((playerArrived && enemyArrived) && !logSaved) {
       gameFinished = true;
       logSaved = true;
       // 게임 종료 처리
   }
   ```

2. **GameController.java** - 응답에 메시지 추가
   ```java
   response.put("passedMessage", "플레이어 통과!");
   response.put("gameFinished", status.gameFinished);
   ```

3. **Maze_swing.java** - UI 메시지 변경
   ```java
   // 첫 도착 시
   if (result.arrived && !playerArrived) {
       playerArrived = true;
       msgLabel.setText("플레이어 통과!");  // 종료 아닌 통과 메시지
   }
   
   // 두 번째 도착 시
   if ((playerArrived && enemyArrived) && !gameFinished) {
       gameFinished = true;
       enemyTimer.stop();
       showGameOverDialog("게임 종료!");  // 진짜 종료
   }
   ```

### 8.4 플래그 변수 상태 정리

| playerArrived | enemyArrived | gameFinished | 동작 |
|---------------|--------------|--------------|------|
| false | false | false | 게임 진행 중 |
| true | false | false | 플레이어 통과, 게임 계속 |
| false | true | false | Enemy 통과, 게임 계속 |
| true | true | true | 게임 종료 |

---

## 9. 로그 파일 포맷 문제

### 9.1 현재 상태

로그 파일은 `ObjectOutputStream`으로 직렬화되어 저장됩니다. 따라서 일반 텍스트 편집기로 열면 16진수만 보입니다.

**저장 위치**: `game_logs/game_YYYYMMDD_HHMMSS_xxxx.log`

### 9.2 로그 구조 (직렬화 클래스)

| 클래스 | 설명 |
|--------|------|
| `GameLog` | 게임 전체 로그 (gameId, 미로, 이벤트 리스트) |
| `GameEvent` | 단일 이벤트 (timestamp, eventType, direction, 좌표, 메시지) |

### 9.3 로그 파일을 텍스트로 보기

**방법 1: 웹에서 리플레이**
```
http://localhost:8080/replay
```
로그 파일 선택 후 재생

**방법 2: Java로 직접 읽기**
```java
GameLogger logger = new GameLogger();
GameLog log = logger.loadLog("game_logs/game_xxxx.log");
System.out.println(log.getGameId());
log.getEvents().forEach(e -> System.out.println(e));
```

### 9.4 권장 개선

직렬화之外에도 **JSON/텍스트 로그**도 함께 저장:
```java
// GameLogger.java에 추가
private void saveLogAsText(GameLog log, String filename) {
    try (PrintWriter pw = new PrintWriter(filename + ".txt")) {
        pw.println("Game ID: " + log.getGameId());
        pw.println("Maze Size: " + log.getMazeSize());
        for (GameEvent e : log.getEvents()) {
            pw.printf("%d | %s | %s | P(%d,%d) E(%d,%d) | %s%n",
                e.getTimestamp(), e.getEventType(), e.getDirection(),
                e.getPlayerX(), e.getPlayerY(), e.getEnemyX(), e.getEnemyY(),
                e.getMessage());
        }
    }
}
```

---

## 9.5 듀얼 플레이어 골인 처리 구현 (2026-03-11)

### 요구사항

| 상황 | 원하는 동작 |
|------|------------|
| 한 명만 도착 | "플레이어/Enemy 통과" 메시지 표시, 게임 계속 |
| 둘 다 도착 | 게임 종료 modal 표시 |

### 구현 코드

#### MazeService.java - 게임 종료 로직

**플레이어 이동 시 (라인 184-198):**
```java
// 도착했는지 확인
if (result.arrived && !playerArrived) {
    playerArrived = true;
    if (playerArrived && enemyArrived) {
        gameFinished = true;
        logSaved = true;
        String logFile = gameLogger.finishAndSave(playerArrived, enemyArrived);
    }
}
```

**AI 이동 시 (라인 222-239):**
```java
if (map[ex][ey] == 9 && !enemyArrived) {
    enemyArrived = true;
    if (playerArrived && enemyArrived) {
        gameFinished = true;
        logSaved = true;
        String logFile = gameLogger.finishAndSave(playerArrived, enemyArrived);
    }
}
```

#### GameStatus 클래스 (라인 282-293)
```java
public static class GameStatus {
    public String mazeView;
    public int playerX, playerY;
    public int enemyX, enemyY;
    public boolean gameStarted;
    public boolean gameFinished;
    public boolean playerArrived;  // 추가
    public boolean enemyArrived;   // 추가
}
```

#### game.html - 프론트엔드 분기

**플레이어 이동 시:**
```javascript
if (data.arrived) {
    if (data.gameFinished) {
        // 둘 다 도착: modal 표시
        gameOverModal.style.display = 'flex';
    } else {
        // 한 명만 도착: 통과 메시지
        let passMessage = data.playerArrived ? "🎯 플레이어 통과!" : "🤖 AI 통과!";
        messageBox.textContent = passMessage;
    }
}
```

**AI 이동 시 (버그 수정):**
```javascript
if (data.enemyArrived) {
    clearTimeout(aiMoveInterval);  // AI 이동 중단
    
    if (!data.playerArrived) {
        messageBox.textContent = "🤖 AI 통과!";
    }
    if (data.gameFinished) {
        location.reload();
    }
    return;
}
```

### 플래그 상태

| playerArrived | enemyArrived | gameFinished | 동작 |
|---------------|--------------|--------------|------|
| false | false | false | 게임 진행 |
| true | false | false | 플레이어 통과, 계속 |
| false | true | false | AI 통과, 계속 (AI 중단) |
| true | true | true | 게임 종료 |

---

## 10. 긍정적 평가

- **알고리즘**: Prim's 미로 생성, 오른손 법칙 AI, 루프 방지 로직이 잘 구현됨
- **로그 시스템**: 직렬화 기반 리플레이 시스템이 견고함
- **아이템 시스템**: 횃불/망치/함정이 잘 작동함
- **구조**: Spring Boot 기반 REST API 구조가 잘 되어 있음
