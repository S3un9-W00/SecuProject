
## Issue: Generated Mazes Too Short

### Problem
- DFS carving creates mazes but paths are too short
- Test results show paths of 2-26 cells when 20-40 required
- Both starts fail to meet N*2 requirement

### Root Cause Analysis
1. DFS starts from only first start point
2. Second start may not be well-connected to DFS tree
3. 2-cell jumps create sparse mazes (many walls, few passages)
4. Goal placement fallback accepts ANY cell when no candidates meet requirement

### Test Results
```
10x10: Start1->Goal: 2, Start2->Goal: 8 (need >= 20)
15x15: Start1->Goal: 26, Start2->Goal: 6 (need >= 30)
20x20: Start1->Goal: 8, Start2->Goal: 20 (need >= 40)
```

### Possible Solutions
1. Carve from both starts (merge DFS trees)
2. Reduce wall density (more passages)
3. Add winding corridors after initial generation
4. Reject mazes that don't meet requirement (stricter validation)
