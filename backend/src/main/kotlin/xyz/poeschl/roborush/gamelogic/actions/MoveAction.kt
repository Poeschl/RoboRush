package xyz.poeschl.roborush.gamelogic.actions

import com.fasterxml.jackson.annotation.JsonCreator
import org.slf4j.LoggerFactory
import xyz.poeschl.roborush.exceptions.InsufficientFuelException
import xyz.poeschl.roborush.gamelogic.GameHandler
import xyz.poeschl.roborush.models.ActiveRobot
import xyz.poeschl.roborush.models.Direction
import xyz.poeschl.roborush.models.Position

class MoveAction @JsonCreator constructor(val direction: Direction) : RobotAction<Position> {

  companion object {
    private val LOGGER = LoggerFactory.getLogger(MoveAction::class.java)
  }

  override fun check(robot: ActiveRobot, gameHandler: GameHandler) {
    val currentPosition = robot.position
    val newPosition = getResultPosition(currentPosition)

    LOGGER.debug("Check {} -> {}", currentPosition, newPosition)
    gameHandler.checkIfPositionIsValidForMove(newPosition)

    val fuelCost = gameHandler.getFuelCostForMove(currentPosition, newPosition)
    if (fuelCost > robot.fuel) {
      throw InsufficientFuelException("The available fuel (${robot.fuel}) is insufficient for the journey. Required fuel: $fuelCost ")
    }
  }

  override fun action(robot: ActiveRobot, gameHandler: GameHandler): Position {
    val currentPosition = robot.position
    val newPosition = getResultPosition(currentPosition)
    LOGGER.debug("{} -> {}", currentPosition, newPosition)

    val fuelCost = gameHandler.getFuelCostForMove(currentPosition, newPosition)

    robot.fuel -= fuelCost
    robot.position = newPosition
    robot.knownPositions.add(newPosition)
    LOGGER.debug("Moved robot {} {} -> {}", robot.id, currentPosition, newPosition)
    gameHandler.sendRobotUpdate(robot)

    return newPosition
  }

  fun getResultPosition(currentPosition: Position) = when (direction) {
    Direction.EAST -> currentPosition.eastPosition()
    Direction.WEST -> currentPosition.westPosition()
    Direction.NORTH -> currentPosition.northPosition()
    Direction.SOUTH -> currentPosition.southPosition()
  }

  override fun toString(): String {
    return "Move(direction=$direction)"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as MoveAction

    return direction == other.direction
  }

  override fun hashCode(): Int {
    return direction.hashCode()
  }
}
