package xyz.poeschl.roborush.gamelogic.dummybots

import xyz.poeschl.roborush.exceptions.InsufficientFuelException
import xyz.poeschl.roborush.exceptions.PositionNotAllowedException
import xyz.poeschl.roborush.exceptions.PositionOutOfMapException
import xyz.poeschl.roborush.exceptions.TankFullException
import xyz.poeschl.roborush.gamelogic.GameHandler
import xyz.poeschl.roborush.gamelogic.actions.MoveAction
import xyz.poeschl.roborush.gamelogic.actions.SolarChargeAction
import xyz.poeschl.roborush.models.ActiveRobot
import xyz.poeschl.roborush.models.Direction
import xyz.poeschl.roborush.models.Position
import xyz.poeschl.roborush.repositories.Robot
import kotlin.math.sqrt

/**
 * A sophisticated bot that uses A* pathfinding algorithm to find optimal routes.
 * This bot demonstrates how to implement proper pathfinding that considers:
 * - Height differences and fuel costs
 * - Obstacles and other robots
 * - Efficient path planning
 * 
 * This is an example of how players should approach pathfinding instead of 
 * using simple greedy approaches.
 */
class SmartPathfindingBot(private val gameHandler: GameHandler, robot: Robot) : Bot(gameHandler, robot) {

  data class PathNode(
    val position: Position,
    val gScore: Double, // Cost from start to this node
    val fScore: Double, // Estimated total cost through this node
    val parent: PathNode? = null
  )

  override fun makeMove(activeRobot: ActiveRobot) {
    if (activeRobot.nextAction == null && activeRobot.fuel > 0) {
      var moveValid = false
      var triesLeft = 10

      while (!moveValid && triesLeft > 0) {
        try {
          val target = gameHandler.getTargetPosition()
          val path = findOptimalPath(activeRobot.position, target, activeRobot)
          
          if (path.isNotEmpty()) {
            val nextPosition = path[0]
            val direction = getDirectionTo(activeRobot.position, nextPosition)
            gameHandler.nextActionForRobot(activeRobot.id, MoveAction(direction))
            moveValid = true
          } else {
            // Fallback to simple movement if pathfinding fails
            val fallbackMove = getSimpleFallbackMove(activeRobot)
            if (fallbackMove != null) {
              gameHandler.nextActionForRobot(activeRobot.id, fallbackMove)
              moveValid = true
            }
          }
        } catch (_: PositionNotAllowedException) {
          // Position blocked, try alternative approach
        } catch (_: PositionOutOfMapException) {
          // Out of bounds, skip this try
        } catch (_: InsufficientFuelException) {
          try {
            gameHandler.nextActionForRobot(activeRobot.id, SolarChargeAction())
            moveValid = true
          } catch (_: TankFullException) {
            // Tank full, can't charge more
          }
        }
        triesLeft--
      }
    }
  }

  /**
   * Implements A* pathfinding algorithm considering fuel costs and height differences
   */
  private fun findOptimalPath(start: Position, target: Position, robot: ActiveRobot): List<Position> {
    val openSet = mutableListOf<PathNode>()
    val closedSet = mutableSetOf<Position>()
    val startNode = PathNode(start, 0.0, heuristic(start, target))
    
    openSet.add(startNode)
    
    while (openSet.isNotEmpty()) {
      // Get node with lowest f-score
      val current = openSet.minByOrNull { it.fScore } ?: break
      openSet.remove(current)
      
      if (current.position == target) {
        // Reconstruct path
        return reconstructPath(current)
      }
      
      closedSet.add(current.position)
      
      // Check all neighbors
      for (neighbor in getValidNeighbors(current.position, robot)) {
        if (neighbor in closedSet) continue
        
        val moveCost = getMoveCost(current.position, neighbor, robot)
        val tentativeGScore = current.gScore + moveCost
        
        val existingNode = openSet.find { it.position == neighbor }
        if (existingNode == null) {
          // New node
          val newNode = PathNode(
            position = neighbor,
            gScore = tentativeGScore,
            fScore = tentativeGScore + heuristic(neighbor, target),
            parent = current
          )
          openSet.add(newNode)
        } else if (tentativeGScore < existingNode.gScore) {
          // Better path found
          openSet.remove(existingNode)
          val improvedNode = PathNode(
            position = neighbor,
            gScore = tentativeGScore,
            fScore = tentativeGScore + heuristic(neighbor, target),
            parent = current
          )
          openSet.add(improvedNode)
        }
      }
    }
    
    return emptyList() // No path found
  }

  private fun reconstructPath(node: PathNode): List<Position> {
    val path = mutableListOf<Position>()
    var current: PathNode? = node
    
    while (current?.parent != null) {
      path.add(0, current.position)
      current = current.parent
    }
    
    return path
  }

  private fun getValidNeighbors(position: Position, robot: ActiveRobot): List<Position> {
    val neighbors = listOf(
      position.northPosition(),
      position.southPosition(),
      position.eastPosition(),
      position.westPosition()
    )
    
    return neighbors.filter { neighbor ->
      try {
        gameHandler.checkIfPositionIsValidForMove(neighbor)
        val fuelCost = gameHandler.getFuelCostForMove(position, neighbor)
        fuelCost <= robot.fuel // Can afford the move
      } catch (e: Exception) {
        false
      }
    }
  }

  private fun getMoveCost(from: Position, to: Position, robot: ActiveRobot): Double {
    return try {
      gameHandler.getFuelCostForMove(from, to).toDouble()
    } catch (e: Exception) {
      Double.MAX_VALUE // Invalid move
    }
  }

  private fun heuristic(from: Position, to: Position): Double {
    // Manhattan distance with slight preference for straight lines
    val dx = kotlin.math.abs(to.x - from.x)
    val dy = kotlin.math.abs(to.y - from.y)
    return (dx + dy).toDouble() + 0.001 * sqrt((dx * dx + dy * dy).toDouble())
  }

  private fun getDirectionTo(from: Position, to: Position): Direction {
    return when {
      to.x > from.x -> Direction.EAST
      to.x < from.x -> Direction.WEST
      to.y > from.y -> Direction.SOUTH
      to.y < from.y -> Direction.NORTH
      else -> Direction.NORTH // Default fallback
    }
  }

  private fun getSimpleFallbackMove(activeRobot: ActiveRobot): MoveAction? {
    val current = activeRobot.position
    val target = gameHandler.getTargetPosition()
    
    // Try to move towards target with fuel cost consideration
    val possibleMoves = listOf(
      Pair(Direction.EAST, current.eastPosition()),
      Pair(Direction.WEST, current.westPosition()),
      Pair(Direction.SOUTH, current.southPosition()),
      Pair(Direction.NORTH, current.northPosition())
    ).filter { (_, pos) ->
      try {
        gameHandler.checkIfPositionIsValidForMove(pos)
        val fuelCost = gameHandler.getFuelCostForMove(current, pos)
        fuelCost <= activeRobot.fuel
      } catch (e: Exception) {
        false
      }
    }.sortedBy { (_, pos) ->
      // Sort by distance to target
      pos.getDistanceTo(target)
    }
    
    return if (possibleMoves.isNotEmpty()) {
      MoveAction(possibleMoves.first().first)
    } else {
      null
    }
  }
}