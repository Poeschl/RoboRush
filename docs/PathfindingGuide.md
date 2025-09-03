# RoboRush Pathfinding Strategy Guide

## The Problem with Simple Approaches

Many new players attempt to win RoboRush by using simple greedy algorithms like "always move left and down toward the target." While this might seem logical, it's actually a **suboptimal strategy** that will lose against properly implemented pathfinding.

## Why Simple Greedy Fails

### 1. Ignores Fuel Costs
In RoboRush, movement costs depend on terrain height:
- **Moving uphill**: costs `1 + height_difference` fuel
- **Moving downhill**: costs only `1` fuel (base cost)

A greedy algorithm that just moves toward the target might:
- Take a path over a mountain (expensive)
- Instead of going around through a valley (cheaper)

### 2. No Obstacle Avoidance
Simple approaches don't handle:
- Blocked positions (other robots)
- Out-of-bounds areas
- Strategic positioning

### 3. Lack of Future Planning
Greedy algorithms make locally optimal choices that lead to globally suboptimal results.

## Recommended Pathfinding Algorithms

### A* Algorithm (Recommended)
A* is the gold standard for pathfinding in games like RoboRush because it:
- Finds optimal paths
- Considers both distance and cost
- Handles complex terrain efficiently

**Key Components:**
- **G-score**: Actual cost from start to current node
- **H-score**: Heuristic estimate from current node to goal
- **F-score**: G + H (total estimated cost)

### Implementation Strategy

```pseudocode
function findPath(start, goal):
    openSet = [start]
    closedSet = []
    
    while openSet is not empty:
        current = node in openSet with lowest f-score
        
        if current == goal:
            return reconstructPath(current)
        
        move current from openSet to closedSet
        
        for each neighbor of current:
            if neighbor in closedSet:
                continue
            
            tentativeG = current.g + moveCost(current, neighbor)
            
            if neighbor not in openSet:
                add neighbor to openSet
            else if tentativeG >= neighbor.g:
                continue
            
            neighbor.parent = current
            neighbor.g = tentativeG
            neighbor.f = neighbor.g + heuristic(neighbor, goal)
    
    return null // No path found
```

## Fuel Cost Calculation

The key to good pathfinding in RoboRush is properly calculating movement costs:

```pseudocode
function moveCost(fromPosition, toPosition):
    staticCost = 1
    heightDifference = getHeight(toPosition) - getHeight(fromPosition)
    
    if heightDifference > 0:
        // Going uphill costs extra
        return staticCost + heightDifference
    else:
        // Going downhill or flat is just base cost
        return staticCost
```

## Heuristic Functions

For the A* algorithm, you need a heuristic that estimates the remaining cost:

### Manhattan Distance (Good for grid-based movement)
```pseudocode
function manhattanHeuristic(from, to):
    return abs(to.x - from.x) + abs(to.y - from.y)
```

### Euclidean Distance (Better for varied terrain)
```pseudocode
function euclideanHeuristic(from, to):
    dx = to.x - from.x
    dy = to.y - from.y
    return sqrt(dx*dx + dy*dy)
```

### Enhanced Heuristic (Recommended)
```pseudocode
function enhancedHeuristic(from, to):
    euclidean = euclideanDistance(from, to)
    manhattan = manhattanDistance(from, to)
    
    // Slight preference for straight lines
    return euclidean + 0.001 * manhattan
```

## Advanced Strategies

### 1. Map Exploration
- Use scan actions to learn terrain
- Build a map of known heights
- Update pathfinding as you learn more

### 2. Dynamic Replanning
- Recalculate paths when:
  - New obstacles appear
  - Better routes are discovered
  - Fuel situation changes

### 3. Fuel Management
- Consider fuel stations in pathfinding
- Plan routes that include refueling stops
- Use solar charging strategically

### 4. Competitive Positioning
- Block opponent routes when possible
- Choose paths that minimize vulnerability
- Consider endgame positioning

## Common Mistakes to Avoid

### ❌ Pure Greedy Algorithms
```pseudocode
// BAD: This ignores fuel costs and obstacles
if current.x < target.x:
    move EAST
elif current.x > target.x:
    move WEST
elif current.y < target.y:
    move SOUTH
elif current.y > target.y:
    move NORTH
```

### ❌ Ignoring Height Differences
```pseudocode
// BAD: All moves treated as equal cost
cost = 1  // This is wrong!
```

### ❌ No Fallback Strategy
```pseudocode
// BAD: What happens if pathfinding fails?
path = findPath(current, target)
move(path[0])  // Could crash if path is null
```

## Best Practices

### ✅ Robust Pathfinding
```pseudocode
function makeMove():
    path = findOptimalPath(current, target)
    
    if path and len(path) > 1:
        nextMove = path[1]
        move(nextMove)
    else:
        // Fallback strategy
        makeSmartFallbackMove()
```

### ✅ Cost-Aware Movement
```pseudocode
function getMoveCost(from, to):
    baseCost = 1
    heightDiff = getHeight(to) - getHeight(from)
    
    // Only add cost for going uphill
    return baseCost + max(0, heightDiff)
```

### ✅ Error Handling
```pseudocode
function attemptMove(direction):
    try:
        sendMoveAction(direction)
    catch InsufficientFuelException:
        // Try to charge or find alternative
        chargeOrFindAlternative()
    catch PositionBlockedException:
        // Recalculate path
        recalculatePath()
```

## Example Implementations

See the example robots in the `example-robots` directory:
- `SmartPathfindingRobot.java` - Java implementation of A* pathfinding
- `SmartPathfindingRobot.py` - Python implementation with detailed comments

These examples show complete implementations of the strategies described in this guide.

## Testing Your Implementation

1. **Test against dummy bots** - The game includes several dummy bots with different strategies
2. **Try different map types** - Test on maps with various terrain features
3. **Measure performance** - Track win rates and fuel efficiency
4. **Iterate and improve** - Continuously refine your pathfinding algorithm

## Conclusion

Successful RoboRush players use sophisticated pathfinding algorithms that consider:
- Terrain height and fuel costs
- Obstacle avoidance
- Strategic positioning
- Robust error handling

Simple "left and down" strategies might work occasionally, but they'll consistently lose to properly implemented A* pathfinding. Invest time in learning these algorithms - the competitive advantage is significant!