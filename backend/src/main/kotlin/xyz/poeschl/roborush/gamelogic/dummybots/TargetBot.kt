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

/**
 * An improved target bot that considers fuel costs and height differences.
 * This version demonstrates why simple greedy approaches are suboptimal
 * by implementing basic fuel-aware pathfinding.
 * 
 * NOTE: For better pathfinding, see SmartPathfindingBot which implements A* algorithm.
 */
class TargetBot(private val gameHandler: GameHandler, robot: Robot) : Bot(gameHandler, robot) {

  override fun makeMove(activeRobot: ActiveRobot) {
    if (activeRobot.nextAction == null && activeRobot.fuel > 0) {
      var moveValid = false
      var triesLeft = 10

      while (!moveValid && triesLeft > 0) {
        try {
          val current = activeRobot.position
          val target = gameHandler.getTargetPosition()
          
          // Get the best move considering fuel costs (improved from simple greedy approach)
          val attemptedMove = getBestMoveToTarget(current, target, activeRobot)
          
          gameHandler.nextActionForRobot(activeRobot.id, attemptedMove)
          moveValid = true
        } catch (_: PositionNotAllowedException) {
          // Position blocked, will try alternative in getBestMoveToTarget
        } catch (_: PositionOutOfMapException) {
          continue
        } catch (_: InsufficientFuelException) {
          try {
            gameHandler.nextActionForRobot(activeRobot.id, SolarChargeAction())
            moveValid = true
          } catch (_: TankFullException) {
            // Tank is full, but still insufficient fuel for any move
          }
        }
        triesLeft--
      }
    }
  }

  /**
   * Improved move selection that considers fuel costs instead of just moving toward target.
   * This shows why simple x/y comparison is insufficient for good pathfinding.
   */
  private fun getBestMoveToTarget(current: Position, target: Position, robot: ActiveRobot): MoveAction {
    // Calculate potential moves and their costs
    val possibleMoves = listOf(
      Direction.EAST to current.eastPosition(),
      Direction.WEST to current.westPosition(),
      Direction.SOUTH to current.southPosition(),
      Direction.NORTH to current.northPosition()
    ).mapNotNull { (direction, position) ->
      try {
        gameHandler.checkIfPositionIsValidForMove(position)
        val fuelCost = gameHandler.getFuelCostForMove(current, position)
        val distanceToTarget = position.getDistanceTo(target)
        
        // Can we afford this move?
        if (fuelCost <= robot.fuel) {
          Triple(direction, fuelCost, distanceToTarget)
        } else {
          null
        }
      } catch (e: Exception) {
        null // Invalid move
      }
    }

    if (possibleMoves.isEmpty()) {
      // Fallback to old simple approach if no valid moves found
      return when {
        (current.x < target.x) -> MoveAction(Direction.EAST)
        (current.x > target.x) -> MoveAction(Direction.WEST)
        (current.y < target.y) -> MoveAction(Direction.SOUTH)
        (current.y > target.y) -> MoveAction(Direction.NORTH)
        else -> MoveAction(Direction.NORTH)
      }
    }

    // Sort by: 1) Distance to target (primary), 2) Fuel cost (secondary)
    // This is still not optimal but better than pure greedy approach
    val bestMove = possibleMoves.minWithOrNull { a, b ->
      val distanceComparison = a.third.compareTo(b.third)
      if (distanceComparison == 0) {
        a.second.compareTo(b.second) // Prefer lower fuel cost if distance is same
      } else {
        distanceComparison
      }
    }

    return MoveAction(bestMove?.first ?: Direction.NORTH)
  }
}
