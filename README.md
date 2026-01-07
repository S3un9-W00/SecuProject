# 미로찾기 게임 프로젝트

## 프로젝트 개요

텍스트 파일 기반 멀티플레이어 미로 게임 프로젝트입니다. Java를 사용하여 콘솔, Swing GUI, 그리고 Spring Boot 웹 애플리케이션으로 단계적으로 개발되었습니다.

### 개발 단계
1. **Maze_one**: 콘솔 기반 기본 미로 게임
2. **Maze_two**: 개선된 미로 게임 클래스 (안개 시스템, 아이템 효과)
3. **Maze_swing**: Swing GUI 기반 미로 게임 (Enemy AI 포함)
4. **최종**: Spring Boot 웹 애플리케이션

---

## 게임 규칙

### 미로 구성 요소
- **0**: 스타트 지점
- **1**: 나 플레이어
- **2**: 너 플레이어 (AI)
- **3**: 길 (이동 가능)
- **4**: 벽 (이동 불가)
- **5**: 안개
- **6**: 아이템 (3×3 또는 5×5 범위 스캔, 5초간)
- **9**: 도착지점

### 게임 플레이
- **조작**: W(위), A(왼쪽), S(아래), D(오른쪽)
- **목표**: 도착지점(9)에 도달
- **안개 시스템**: 플레이어 주변 3×3 범위만 보임 (아이템 사용 시 5×5)
- **AI**: Enemy가 오른손 법칙으로 자동 이동

---

## 프로젝트 구조

```
src/main/java/com/example/secuproject/
├── Maze_one.java          # 1단계: 콘솔 기반 미로 게임
├── Maze_two.java          # 2단계: 개선된 미로 클래스
├── Maze_swing.java        # 3단계: Swing GUI 게임
├── Enemy.java             # 오른손 법칙 AI 로봇
├── Service/
│   └── MazeService.java   # Spring 서비스 레이어
├── controller/
│   └── GameController.java # Spring 컨트롤러
├── model/                 # 데이터 모델
│   ├── Maze.java
│   ├── MazeCell.java
│   ├── Player.java
│   └── Position.java
├── generator/
│   └── MazeGenerator.java # 미로 자동 생성
├── game/
│   └── GameEngine.java    # 게임 엔진
├── ai/
│   └── AIRobot.java       # AI 로봇 (오른손 법칙)
└── util/
    └── MazeFileHandler.java # 파일 입출력
```

---

## 각 단계별 설명

### 1단계: Maze_one (콘솔 기반 미로 게임)

**개요**: 가장 기본적인 콘솔 기반 미로 게임입니다.

**주요 기능**:
- 5×5 고정 크기 미로
- WASD 키로 플레이어 이동

**개발 시 고려사항**:
- 간단한 구조로 게임 로직 이해
- Scanner를 사용한 콘솔 입력 처리
- 기본적인 이동 검증 로직

**주요 로직**:
```java
// 이동 처리
public boolean move(char c) {
    // 방향에 따른 좌표 계산
    // 범위 및 벽 체크
    // 도착지점 확인
    // 아이템 효과 처리
}
```

---

### 2단계: Maze_two (개선된 미로 클래스)

**개요**: Maze_one을 개선하여 재사용 가능한 클래스로 만든 버전입니다. 또, 안개와 아이템 기능이 추가되었습니다.

**주요 개선사항**:
- `MoveResult` 클래스로 이동 결과 구조화
- `showMaze()` 메서드가 String 반환 (콘솔 출력 분리)
- 아이템 효과 시간 관리 개선 (5초 타이머)
- 안개 시스템 개선 (3×3 범위)

**개발 시 고려사항**:
- 객체 지향 설계 원칙 적용
- 메서드 분리로 재사용성 향상
- 상태 관리 개선 (itemEffect, endTime)

**주요 로직**:
```java
// 미로 표시 (문자열 반환)
public String showMaze() {
    // 아이템 효과 시간 체크
    // 플레이어 주변 시야 계산
    // 미로 상태를 문자열로 변환
}

// 이동 결과 구조화
public static class MoveResult {
    boolean moved;      // 이동 성공 여부
    boolean arrived;    // 도착 여부
    String message;     // 결과 메시지
}
```

---

### 3단계: Maze_swing (Swing GUI 게임)

**개요**: Swing을 사용한 그래픽 사용자 인터페이스 미로 게임입니다. 상대방의 움직임을 테스트하기위해 만들어졌습니다.

**주요 기능**:
- Swing JFrame 기반 GUI
- 실시간 미로 렌더링
- Enemy AI 자동 이동 (0.3초마다)
- 키보드 입력으로 플레이어 이동

**개발 시 고려사항**:
- Swing 이벤트 처리 (KeyListener)
- Timer를 사용한 Enemy 자동 이동
- Graphics를 사용한 미로 그리기
- 좌표 변환 (map[행][열] ↔ 화면 좌표)

**주요 로직**:
```java
// Enemy 오른손 법칙
public void step() {
    // 1. 오른쪽 확인
    // 2. 앞 확인
    // 3. 왼쪽 확인
    // 4. 뒤 확인 (유턴)
    // 이동 가능한 방향으로 이동
}

// 미로 그리기
protected void paintComponent(Graphics g) {
    // 미로 셀별 색상 설정
    // 플레이어 위치 표시
    // Enemy 위치 표시
}
```

**오른손 법칙 알고리즘**:
- 현재 방향 기준으로 오른쪽 → 앞 → 왼쪽 → 뒤 순서로 이동 가능한 방향 탐색
- 벽을 따라가면서 미로를 탐색하는 전통적인 알고리즘

---

### 4단계: Spring Boot 웹 애플리케이션

**개요**: Spring Boot를 사용한 웹 기반 미로 게임입니다. Maze_two와 Enemy 클래스만 사용하여 간단하게 구현했습니다.

**주요 기능**:
- RESTful API 엔드포인트
- Thymeleaf 템플릿 엔진
- Maze_two와 Enemy 클래스만 사용 (기존 클래스 재사용)
- 간단한 게임 상태 관리
- **미로 제네레이터**: 랜덤 미로 생성 및 txt 파일 저장
- **미로 검증기**: 생성된 미로의 유효성 검증
- **txt 파일 읽기**: 미리 생성된 미로 파일을 읽어서 게임에 사용

**아키텍처**:
- **Controller**: HTTP 요청 처리 (GameController)
- **Service**: 비즈니스 로직 (MazeService - Maze_two, Enemy만 사용)
- **템플릿**: Thymeleaf로 미로 텍스트 표시
- **미로 제네레이터**: MazeGenerator - 랜덤 미로 생성 및 txt 파일 저장
- **미로 검증기**: MazeValidator - 미로 유효성 검증

**개발 시 고려사항**:
- Maze_two와 Enemy 클래스만 사용하여 최대한 단순화
- 복잡한 모델 클래스 제거 (Maze, Player, Position 등 사용 안 함)
- Maze_two.showMaze() 결과를 텍스트로 그대로 표시
- Enemy.step()으로 오른손 법칙 AI 이동
- Spring Boot 의존성 주입으로 Service 관리

**주요 엔드포인트**:
```
GET  /              # 메인 페이지
POST /game/start    # 게임 시작 (Maze_two 생성, Enemy 배치)
GET  /game          # 게임 화면 (Maze_two.showMaze() 텍스트 표시)
POST /game/move     # 플레이어 이동 (Maze_two.move() 사용, AI도 자동 이동)
POST /game/ai-move  # AI만 이동 (Enemy.step() 사용)
POST /game/reset    # 게임 리셋
GET  /game/status   # 게임 상태 조회
```

**주요 로직**:
```java
// MazeService - Maze_two와 Enemy만 사용
@Service
public class MazeService {
    private Maze_two maze;
    private Enemy enemy;
    private MazeGenerator generator;
    private MazeValidator validator;
    
    public void startGame() {
        // txt 파일에서 미로 읽기
        try {
            maze = Maze_two.fromFile("maze.txt");
            // 미로 검증
            MazeValidator.ValidationResult result = validator.validate(maze.getMap());
            if (!result.valid) {
                maze = new Maze_two(); // 검증 실패 시 기본 미로
            }
        } catch (IOException e) {
            maze = new Maze_two(); // 파일 읽기 실패 시 기본 미로
        }
        
        // Enemy 시작 위치 찾기
        int enemyX = findEnemyStartX();
        int enemyY = findEnemyStartY();
        enemy = new Enemy(maze, enemyX, enemyY, maze.getMap());
    }
    
    public Maze_two.MoveResult move(char dir) {
        // Maze_two의 move() 메서드 사용
        Maze_two.MoveResult result = maze.move(dir);
        if (result.arrived) {
            gameFinished = true;
        }
        return result;
    }
    
    public void aiMove() {
        // Enemy의 step() 메서드로 오른손 법칙 이동
        if (enemy != null && !gameFinished) {
            enemy.step();
            // 도착지점 확인
            int[][] map = maze.getMap();
            if (map[enemy.getX()][enemy.getY()] == 9) {
                gameFinished = true;
            }
        }
    }
}
```

**미로 제네레이터 사용법**:
```java
// MazeGenerator의 main 함수로 미로 생성 및 저장
public static void main(String[] args) {
    MazeGenerator generator = new MazeGenerator();
    MazeValidator validator = new MazeValidator();
    
    // 미로 생성 (크기 10)
    int[][] map = generator.generateMaze(10);
    
    // 미로 검증
    ValidationResult result = validator.validate(map);
    if (result.valid) {
        // txt 파일로 저장
        generator.saveToFile(map, "maze.txt");
    }
}
```

**실행 방법**:
```bash
# 미로 생성 (크기 10, 파일명 maze.txt)
java com.example.secuproject.util.MazeGenerator 10 maze.txt

# 또는 크기만 지정 (기본 파일명 maze.txt)
java com.example.secuproject.util.MazeGenerator 15
```

**특징**:
- Maze_two와 Enemy 클래스의 기존 함수만 사용
- 추가적인 복잡한 로직 없이 단순하게 구현
- 템플릿에서 Maze_two.showMaze() 결과를 텍스트로 표시
- JavaScript로 정확히 0.5초마다 AI 자동 이동 (플레이어 이동 시에는 AI가 이동하지 않음)
- **미로 제네레이터**: main 함수로 미로를 생성하고 txt 파일로 저장
- **미로 검증기**: 생성된 미로가 유효한지 검증 (스타트 지점, 도착지점, 경로 확인)
- **파일 기반 미로**: 미리 생성된 maze.txt 파일을 읽어서 게임에 사용
- **Enemy 위치 표시**: 미로에 Enemy 위치를 'E'로 표시
- **AI 루프 방지**: 방문 횟수 추적으로 루프 감지 및 탈출 로직
- **정확한 타이밍 제어**: setTimeout 기반으로 정확한 0.5초 간격 보장

---

## 기술 스택

- **언어**: Java 17
- **프레임워크**: Spring Boot 3.2.0
- **템플릿 엔진**: Thymeleaf
- **GUI**: Java Swing (Maze_swing)
- **빌드 도구**: Gradle

---

## 개발 시 고려했던 점

### 1. 코드 재사용성
- Maze_two 클래스를 모든 단계에서 재사용
- Enemy 클래스를 Swing과 Spring에서 모두 활용
- 공통 로직을 클래스로 분리하여 중복 제거

### 2. 단계적 개발
- 콘솔 → GUI → 웹으로 점진적 발전
- 각 단계에서 이전 코드를 개선하며 재사용
- 점진적 복잡도 증가로 학습 곡선 완화

### 3. 알고리즘 선택
- **오른손 법칙**: 간단하면서도 효과적인 미로 탐색 알고리즘
- 벽을 따라가면서 경로를 찾는 직관적인 방법
- 복잡한 경로 탐색 알고리즘(A*, BFS) 대신 구현이 쉬운 방법 선택

### 4. 안개 시스템
- 플레이어 주변 3×3 범위만 가시화
- 아이템 사용 시 5×5 범위로 확대
- 게임 난이도 조절 및 탐험 요소 추가

### 5. 상태 관리
- MazeService에서 Maze_two와 Enemy 인스턴스 관리
- 게임 시작/종료 상태를 boolean으로 간단하게 관리
- Maze_two와 Enemy의 상태는 각 클래스 내부에서 관리

### 6. 미로 생성 및 검증
- **미로 제네레이터**: 랜덤 미로 생성 알고리즘 구현
  - 스타트 지점 2개, 도착지점, 아이템 자동 배치
  - 경로 보장 로직 (BFS 사용)
- **미로 검증기**: 생성된 미로의 유효성 검증
  - 스타트 지점 존재 확인
  - 도착지점 존재 확인
  - 각 스타트에서 도착지점까지 경로 확인
  - 최소 2개 이상의 경로 보장 확인
- **파일 기반 시스템**: 미로를 txt 파일로 저장하고 읽기
  - 미로 제네레이터의 main 함수로 미로 생성 및 저장
  - Spring에서 txt 파일을 읽어서 게임에 사용

### 7. AI 루프 방지 및 타이밍 제어
- **루프 방지 알고리즘**: 
  - 각 위치의 방문 횟수를 추적하여 루프 감지
  - 같은 위치를 5번 이상 방문하거나 같은 위치에 3번 이상 머무르면 루프로 간주
  - 루프 감지 시 방문 횟수가 적은 곳으로 랜덤 이동하여 탈출
  - 방문 횟수가 적은 위치를 우선적으로 선택 (3회 미만)
  - 막다른 길(뒤로 가는 경우)은 방문 횟수 체크 없이 이동
  - txt 파일로 생성된 복잡한 미로에서도 루프에 빠지지 않고 도착지점 탐색 가능
- **정확한 타이밍 제어**:
  - setInterval 대신 setTimeout 사용
  - 요청 완료 후 다음 요청을 스케줄링하여 네트워크 지연 보정
  - 경과 시간 계산으로 정확한 0.5초 간격 보장

---

## 주요 로직 설명

### 오른손 법칙 (Right-Hand Rule)

Enemy AI가 사용하는 미로 탐색 알고리즘입니다.

```java
public void step() {
    // 방향 배열: 위, 오른쪽, 아래, 왼쪽
    int[] dx = {-1, 0, 1, 0};
    int[] dy = {0, 1, 0, -1};
    
    // 1. 오른쪽 확인
    int rightDir = (dir + 1) % 4;
    if (isFree(x + dx[rightDir], y + dy[rightDir])) {
        // 오른쪽으로 회전 후 이동
        dir = rightDir;
        move(rightDir);
        return;
    }
    
    // 2. 앞 확인
    if (isFree(x + dx[dir], y + dy[dir])) {
        move(dir);
        return;
    }
    
    // 3. 왼쪽 확인
    // 4. 뒤 확인 (유턴)
    // ...
}
```

**동작 원리**:
1. 현재 방향 기준으로 오른쪽 벽을 따라감
2. 오른쪽에 길이 있으면 오른쪽으로 회전
3. 오른쪽이 막혀있으면 앞으로 진행
4. 앞도 막혀있으면 왼쪽, 마지막으로 뒤로 유턴

### AI 루프 방지 알고리즘

오른손 법칙만으로는 일부 미로에서 루프에 빠질 수 있어, 루프 방지 로직을 추가했습니다.

```java
// 방문 횟수 추적 및 같은 위치 머무름 감지
private Map<String, Integer> visitedCount = new HashMap<>();
private int samePositionCount = 0;
private int lastX = -1, lastY = -1;

public void step() {
    // 같은 위치에 머무는지 확인
    if (x == lastX && y == lastY) {
        samePositionCount++;
    } else {
        samePositionCount = 0;
    }
    lastX = x;
    lastY = y;

    // 현재 위치 방문 횟수 증가
    String currentPos = x + "," + y;
    int visitCount = visitedCount.getOrDefault(currentPos, 0) + 1;
    visitedCount.put(currentPos, visitCount);

    // 루프 감지: 같은 위치를 5번 이상 방문하거나, 같은 위치에 3번 이상 머무르면
    if (visitCount > 5 || samePositionCount > 3) {
        // 루프 탈출: 이동 가능한 모든 방향 중 방문 횟수가 적은 곳 우선 선택
        List<int[]> possibleMoves = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];
            if (isFree(nx, ny)) {
                String nextPos = nx + "," + ny;
                int nextVisitCount = visitedCount.getOrDefault(nextPos, 0);
                possibleMoves.add(new int[]{nx, ny, i, nextVisitCount});
            }
        }
        
        if (!possibleMoves.isEmpty()) {
            // 방문 횟수가 적은 곳 우선 선택
            possibleMoves.sort((a, b) -> Integer.compare(a[3], b[3]));
            Random random = new Random();
            int[] move = possibleMoves.get(random.nextInt(Math.min(possibleMoves.size(), 3)));
            x = move[0];
            y = move[1];
            dir = move[2];
            visitedCount.clear(); // 방문 기록 초기화
            samePositionCount = 0;
            return;
        }
    }
    
    // 일반적인 오른손 법칙 로직
    // 방문 횟수가 적은 위치를 우선 선택 (3회 미만)
    if (isFree(rx, ry) && getVisitCount(rx, ry) < 3) {
        // 이동
    }
    
    // 막다른 길(뒤로 가는 경우)은 방문 횟수 체크 없이 이동
    if (isFree(bx, by)) {
        // 이동
    }
}
```

**루프 방지 전략**:
1. 각 위치의 방문 횟수를 Map으로 추적
2. 같은 위치에 머무는 횟수도 별도로 추적
3. 같은 위치를 5번 이상 방문하거나 같은 위치에 3번 이상 머무르면 루프로 간주
4. 루프 감지 시 이동 가능한 모든 방향 중 방문 횟수가 적은 곳 우선 선택
5. 방문 기록 초기화 후 다시 오른손 법칙으로 복귀
6. 일반 이동 시에도 방문 횟수가 3회 미만인 곳 우선 선택
7. 막다른 길(뒤로 가는 경우)은 방문 횟수 체크 없이 이동하여 완전한 막힘 방지

### 정확한 타이밍 제어

JavaScript의 setInterval은 네트워크 지연으로 인해 정확하지 않을 수 있어, setTimeout 기반으로 개선했습니다.

```javascript
// 정확한 0.5초 간격 보장
let lastAiMoveTime = Date.now();
const AI_MOVE_INTERVAL = 500;

function scheduleNextAiMove() {
    const now = Date.now();
    const elapsed = now - lastAiMoveTime;
    const delay = Math.max(0, AI_MOVE_INTERVAL - elapsed);
    
    setTimeout(function() {
        lastAiMoveTime = Date.now();
        
        fetch('/game/ai-move', { ... })
        .then(data => {
            // 요청 완료 후 다음 요청 스케줄링
            scheduleNextAiMove();
        });
    }, delay);
}
```

**타이밍 제어 전략**:
1. 마지막 이동 시간을 기록
2. 경과 시간을 계산하여 정확한 간격 보장
3. 요청 완료 후 다음 요청을 스케줄링
4. 네트워크 지연을 보정하여 일정한 간격 유지

### 안개 시스템

```java
public String showMaze() {
    int viewRange = itemEffect ? 2 : 1; // 아이템 있으면 5×5, 없으면 3×3
    
    for (int i = 0; i < size; i++) {
        for (int j = 0; j < size; j++) {
            boolean inView = Math.abs(i - playerX) <= viewRange &&
                            Math.abs(j - playerY) <= viewRange;
            
            if (!inView) {
                sb.append(" ?"); // 안개
            } else {
                // 실제 미로 상태 표시
            }
        }
    }
}
```

### 아이템 효과

```java
if (map[nx][ny] == 6) {
    itemEffect = true;
    endTime = System.currentTimeMillis() + 5000; // 5초 후 만료
    viewRange = 2; // 5×5 범위로 확대
    map[nx][ny] = 1; // 아이템 제거
}
```

---

## 실행 방법

### 1. 미로 파일 생성

게임을 시작하기 전에 미로 파일을 생성해야 합니다.

```bash
# 프로젝트 루트 디렉토리에서 실행
java com.example.secuproject.util.MazeGenerator 10 maze.txt
```

**파라미터 설명**:
- 첫 번째 인자: 미로 크기 (예: 10 = 10×10 미로)
- 두 번째 인자: 저장할 파일명 (생략 시 기본값: maze.txt)

**예시**:
```bash
# 크기 10의 미로를 maze.txt로 저장
java com.example.secuproject.util.MazeGenerator 10 maze.txt

# 크기 15의 미로를 mymaze.txt로 저장
java com.example.secuproject.util.MazeGenerator 15 mymaze.txt

# 크기만 지정 (기본 파일명 maze.txt 사용)
java com.example.secuproject.util.MazeGenerator 12
```

**생성된 파일**:
- `maze.txt`: 공백으로 구분된 숫자로 구성된 미로 파일
- 각 줄은 미로의 한 행을 나타냄
- 숫자 의미: 0=스타트, 3=길, 4=벽, 6=아이템, 9=도착지점

### 2. Spring Boot 애플리케이션 실행

```bash
# Gradle 사용
./gradlew bootRun

# 또는 JAR 파일로 실행
java -jar build/libs/secuproject-0.0.1-SNAPSHOT.jar
```

웹 브라우저에서 `http://localhost:8080` 접속

### 3. 게임 플레이

1. 메인 페이지에서 "게임 시작" 버튼 클릭
2. Spring이 `maze.txt` 파일을 자동으로 읽어서 게임 시작
3. WASD 키 또는 방향 버튼으로 플레이어 이동
4. AI(Enemy)는 0.5초마다 자동으로 오른손 법칙으로 이동
5. 도착지점(G)에 도달하면 게임 완료

**주의사항**:
- `maze.txt` 파일이 없으면 기본 미로를 사용합니다
- 미로 파일은 프로젝트 루트 디렉토리에 있어야 합니다

---

## 향후 개선 사항

디자인 개선과 코드 리펙토링을 통한 최적화 등을 할 예정입니다. 또, 현재는 오른손의 법칙이 단순하게 적용되었기에 ai가 아직은 모자라기 때문에 이것 또한 개선 예정입니다.



