package xyz.poeschl.roborush.gamelogic.actions

import com.fasterxml.jackson.annotation.JsonCreator
import org.slf4j.LoggerFactory
import xyz.poeschl.roborush.exceptions.InsufficientFuelException
import xyz.poeschl.roborush.gamelogic.GameHandler
import xyz.poeschl.roborush.gamelogic.actions.ScanAction.ScanResult
import xyz.poeschl.roborush.models.ActiveRobot
import xyz.poeschl.roborush.models.Direction
import xyz.poeschl.roborush.models.Position
import xyz.poeschl.roborush.repositories.Tile

class MoveAction @JsonCreator constructor(val direction: Direction) : RobotAction<ScanResult> {

  companion object {
    private val LOGGER = LoggerFactory.getLogger(MoveAction::class.java)
  }

  override fun check(robot: ActiveRobot, gameHandler: GameHandler) {
    val currentPosition = robot.position
    val newPosition = getResultPosition(currentPosition)

    LOGGER.debug("Check {} -> {}", currentPosition, newPosition)
    gameHandler.checkIfPositionIsValidForMove(newPosition)

    val fuelCost = gameHandler.getFuelCostForMove(currentPosition, newPosition)
    if (!robot.hasSufficientFuel(fuelCost)) {
      throw InsufficientFuelException("The available fuel (${robot.fuel}) is insufficient for the journey. Required fuel: $fuelCost ")
    }
  }

  override fun action(robot: ActiveRobot, gameHandler: GameHandler): RobotActionResult<ScanResult> {
    val currentPosition = robot.position
    val newPosition = getResultPosition(currentPosition)
    LOGGER.debug("{} -> {}", currentPosition, newPosition)

    val fuelCost = gameHandler.getFuelCostForMove(currentPosition, newPosition)

    robot.useFuel(fuelCost)
    robot.position = newPosition
    val seenTiles = gameHandler.getTilesForMovementOnPosition(newPosition)
    robot.knownPositions.addAll(seenTiles.map(Tile::position))

    LOGGER.debug("Moved robot {} {} -> {}", robot.id, currentPosition, newPosition)
    gameHandler.sendRobotUpdate(robot)

    return RobotActionResult(robot, ScanResult(seenTiles))
  }

  fun getResultPosition(currentPosition: Position) = when (direction) {
    Direction.EAST -> currentPosition.eastPosition()
    Direction.WEST -> currentPosition.westPosition()
    Direction.NORTH -> currentPosition.northPosition()
    Direction.SOUTH -> currentPosition.southPosition()
  }

  override fun toString(): String = "Move(direction=$direction)"

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as MoveAction

    return direction == other.direction
  }

  override fun hashCode(): Int = direction.hashCode()
}
