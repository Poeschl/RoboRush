package xyz.poeschl.roborush.gamelogic.actions

import com.fasterxml.jackson.annotation.JsonCreator
import org.slf4j.LoggerFactory
import xyz.poeschl.roborush.exceptions.WrongTileTypeException
import xyz.poeschl.roborush.gamelogic.GameHandler
import xyz.poeschl.roborush.models.ActiveRobot
import xyz.poeschl.roborush.models.TileType

class RefuelAction @JsonCreator constructor() : RobotAction<Int> {

  companion object {
    val LOGGER = LoggerFactory.getLogger(RefuelAction::class.java)
  }

  override fun check(robot: ActiveRobot, gameHandler: GameHandler) {
    val currentTileType = gameHandler.getTileAtPosition(robot.position).type

    if (currentTileType != TileType.FUEL_TILE) {
      throw WrongTileTypeException("The robot is not on a fuel tile!")
    }
  }

  override fun action(robot: ActiveRobot, gameHandler: GameHandler): Int {
    robot.fuel = gameHandler.getRobotMaxFuel()
    LOGGER.debug("Refuel robot {} to {}", robot.id, robot.fuel)
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
