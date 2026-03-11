
## Decision: Switch to Recursive Backtracking Algorithm

### Problem with Current Approach
- Random 60% path generation rarely satisfies constraints
- Retry loop (100 attempts) is inefficient
- Wall connectivity with 8-directions is too lenient
- No guarantee of success

### New Approach: Recursive Backtracking (DFS-based)
1. **Start with all walls** (code 4)
2. **Carve passages using DFS** with 2-cell jumps
3. **Wall connectivity guaranteed** by construction (walls are untouched cells)
4. **Long paths naturally created** by DFS deep exploration
5. **4-direction wall connectivity** (stricter, more realistic)

### Algorithm Choice Rationale
- **DFS for generation**: Creates long winding corridors
- **BFS for validation**: Calculates shortest path correctly
- **No wall connectivity check needed**: Guaranteed by construction

### Expected Benefits
- ~90%+ success rate vs ~1% with random
- Faster generation (no retry loop needed)
- More coherent maze structure
- Guaranteed constraint satisfaction

## Decision: Switch from 2-cell to 1-cell Jumps

### Problem with 2-cell Jumps
- 10×10 maze → ~25 passage cells (effective 5×5)
- Need 20-cell path = 80% of all cells in sequence
- Mathematically near-impossible for small mazes

### Solution: 1-cell Jumps
- 10×10 maze → ~100 passage cells
- Need 20-cell path = 20% of cells = achievable
- Walls still connected (uncarved cells)

### Additional Fixes
1. Remove `ensureMultiplePaths()` - creates shortcuts
2. Carve from both starts - better connectivity
3. Remove fallback in goal placement - force regeneration
4. Increase max attempts to 50

### Trade-offs
- Lose thick-wall aesthetic (walls can be 1-cell thick)
- Gain: Reliable N*2 path length satisfaction

## Final Decision: Implement Randomized Prim's Algorithm

### Why Prim's?
1. **Guarantees wall connectivity** - walls form the "background" naturally
2. **Creates winding corridors** - perfect mazes have long paths
3. **Proven algorithm** - standard for maze generation
4. **High success rate** - 90%+ for constraint satisfaction

### Algorithm Overview
1. Start with all walls
2. Pick random start cell, mark as passage
3. Add neighboring walls to frontier
4. While frontier not empty:
   - Pick random wall from frontier
   - If wall connects passage to non-passage:
     - Make wall a passage
     - Add new cell's walls to frontier
5. Result: Perfect maze (all cells connected, no loops)

### Modifications for Our Constraints
- Place 2 starts before algorithm
- Run Prim's to connect entire maze
- Place goal at cell with distance >= N*2 from ALL starts
- Validate and retry if needed
