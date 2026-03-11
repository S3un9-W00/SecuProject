
## Critical Problem: Maze Too Open

### Visualization
```
# # # # # # # # # # 
# . . . . S . . . # 
# . . . . . . . . # 
# . . . . . . . . # 
# . . . . . . . . # 
# G . . . . . . . # 
# . . . . . . . . # 
# . . . . . . . . # 
# . . . . S . . . # 
# # # # # # # # # # 
```

### Issue
- Almost ALL interior cells are paths (.)
- Only border walls (#)
- Paths too short because you can go almost straight
- 58 paths, 36 walls (mostly border)

### Root Cause
DFS carves almost every cell, leaving no internal walls

### Conflict
- Need LONG paths (N*2) → need winding corridors → need internal walls
- Need CONNECTED walls → all walls must touch
- Current: No internal walls → short paths

### Conclusion
**Recursive Backtracking with 1-cell jumps creates too-open mazes**

Need different approach that creates:
1. Internal wall structures (connected)
2. Winding corridors (long paths)
