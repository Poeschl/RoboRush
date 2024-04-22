package xyz.poeschl.pathseeker.gamelogic.actions

import com.fasterxml.jackson.annotation.JsonCreator
import xyz.poeschl.pathseeker.exceptions.WrongTileTypeException
import xyz.poeschl.pathseeker.gamelogic.GameHandler
import xyz.poeschl.pathseeker.models.ActiveRobot
import xyz.poeschl.pathseeker.models.TileType

class RefuelAction @JsonCreator constructor() : RobotAction<Int> {

  override fun check(robot: ActiveRobot, gameHandler: GameHandler) {
    val currentTileType = gameHandler.getTileAtPosition(robot.position).type

    if (currentTileType != TileType.FUEL_TILE) {
      throw WrongTileTypeException("The robot is not on a fuel tile!")
    }
  }

  override fun action(robot: ActiveRobot, gameHandler: GameHandler): Int {
    robot.fuel = gameHandler.getRobotMaxFuel()
    return robot.fuel
  }

  override fun toString(): String {
    return "RefuelAction()"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    return true
  }

  override fun hashCode(): Int {
    return javaClass.hashCode()
  }
}
