# Example robots

This folder contains example robots for different programming languages that demonstrate various approaches to playing RoboRush.

## Important Note About Pathfinding

**Simple greedy approaches like "always go left/down" are suboptimal!** 

The basic examples (AlwaysRightRobot, AlwaysLeftRobot) are intentionally simple to help you understand the API, but they use naive strategies that don't consider:
- Height differences and fuel costs
- Obstacles and blocked paths  
- Optimal route planning

## Robot Examples

### Basic Examples (Learning the API)
- **AlwaysRightRobot.java** - A very simple robot that always moves east
- **AlwaysLeftRobot.py** - A very simple robot that always moves west

These are good for understanding how to communicate with the RoboRush API, but their strategies are intentionally basic.

### Advanced Examples (Competitive Strategies)
- **SmartPathfindingRobot.java** - Demonstrates A* pathfinding algorithm with fuel cost optimization
- **SmartPathfindingRobot.py** - Python implementation of advanced pathfinding techniques

The smart pathfinding robots show how to:
1. Implement A* algorithm for optimal pathfinding
2. Consider height differences that affect fuel consumption
3. Handle obstacles and blocked positions
4. Use proper fallback strategies
5. Make strategic decisions based on map knowledge

## Why Smart Pathfinding Matters

In RoboRush, different terrain heights affect fuel consumption:
- Moving uphill costs extra fuel (1 + height_difference)
- Moving downhill costs only the base fuel (1)
- Running out of fuel means you can't move

Simple strategies that just move "left and down" ignore these costs and often:
- Take suboptimal routes that waste fuel
- Get stuck on high terrain
- Lose to robots using proper pathfinding

## Getting Started

1. **Start with the basic examples** to understand the API
2. **Study the smart pathfinding examples** to learn competitive strategies
3. **Implement your own improvements** based on these patterns
4. **Test against the dummy bots** to see how your strategy performs

## API Communication

All robots access the game information and send actions via REST calls to the RoboRush API. The basic pattern is:

1. Get game state information
2. Register to participate when games are starting
3. Make intelligent moves when it's your turn
4. Reset state when games end

Check the example code for detailed implementation of this communication pattern.
