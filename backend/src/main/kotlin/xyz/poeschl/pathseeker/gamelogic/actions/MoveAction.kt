package xyz.poeschl.pathseeker.gamelogic.actions

import org.slf4j.LoggerFactory
import xyz.poeschl.pathseeker.exceptions.InsufficientFuelException
import xyz.poeschl.pathseeker.gamelogic.GameHandler
import xyz.poeschl.pathseeker.gamelogic.internal.RobotHandler
import xyz.poeschl.pathseeker.models.ActiveRobot
import xyz.poeschl.pathseeker.models.Direction
import xyz.poeschl.pathseeker.models.Position

class MoveAction(private val direction: Direction) : RobotAction<Position> {

  companion object {
    private val LOGGER = LoggerFactory.getLogger(RobotHandler::class.java)
  }
  override fun check(robot: ActiveRobot, gameHandler: GameHandler) {
    val currentPosition = robot.position
    val newPosition =
      when (direction) {
        Direction.EAST -> currentPosition.eastPosition()
        Direction.WEST -> currentPosition.westPosition()
        Direction.NORTH -> currentPosition.northPosition()
        Direction.SOUTH -> currentPosition.southPosition()
      }

    LOGGER.debug("Check {} -> {}", currentPosition, newPosition)
    gameHandler.isPositionValidForMove(newPosition)

    val fuelCost = gameHandler.getFuelCostForMove(currentPosition, newPosition)
    if (fuelCost > robot.fuel) {
      throw InsufficientFuelException("The available fuel (${robot.fuel}) is insufficient for the journey. Required fuel: $fuelCost ")
    }
  }

  override fun action(robot: ActiveRobot, gameHandler: GameHandler): Position {
    val currentPosition = robot.position
    val newPosition =
      when (direction) {
        Direction.EAST -> currentPosition.eastPosition()
        Direction.WEST -> currentPosition.westPosition()
        Direction.NORTH -> currentPosition.northPosition()
        Direction.SOUTH -> currentPosition.southPosition()
      }
    LOGGER.debug("{} -> {}", currentPosition, newPosition)

    val fuelCost = gameHandler.getFuelCostForMove(currentPosition, newPosition)

    robot.fuel -= fuelCost
    robot.position = newPosition
    LOGGER.debug("Moved robot {} {} -> {}", robot.id, currentPosition, newPosition)
    gameHandler.sendRobotUpdate(robot)

    return newPosition
  }

  override fun toString(): String {
    return "Move(direction=$direction)"
  }
}
