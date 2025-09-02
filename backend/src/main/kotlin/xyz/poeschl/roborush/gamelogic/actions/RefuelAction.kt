package xyz.poeschl.roborush.gamelogic.actions

import com.fasterxml.jackson.annotation.JsonCreator
import org.slf4j.LoggerFactory
import xyz.poeschl.roborush.exceptions.TankFullException
import xyz.poeschl.roborush.exceptions.WrongTileTypeException
import xyz.poeschl.roborush.gamelogic.GameHandler
import xyz.poeschl.roborush.gamelogic.actions.RefuelAction.RefuelActionResult
import xyz.poeschl.roborush.models.ActiveRobot
import xyz.poeschl.roborush.models.TileType

class RefuelAction @JsonCreator constructor() : RobotAction<RefuelActionResult> {

  companion object {
    val LOGGER = LoggerFactory.getLogger(RefuelAction::class.java)
  }

  override fun check(robot: ActiveRobot, gameHandler: GameHandler) {
    val currentTileType = gameHandler.getTileAtPosition(robot.position).type
    if (currentTileType != TileType.FUEL_TILE) {
      throw WrongTileTypeException("The robot is not on a fuel tile!")
    }

    if (!robot.canRetrieveFuel()) {
      throw TankFullException("Fuel tank is already full")
    }
  }

  override fun action(robot: ActiveRobot, gameHandler: GameHandler): RobotActionResult<RefuelActionResult> {
    robot.addFuel(gameHandler.getRobotMaxFuel())
    LOGGER.debug("Refuel robot {} to {}", robot.id, robot.fuel)
    return RobotActionResult(robot, RefuelActionResult(robot.fuel))
  }

  override fun toString(): String = "RefuelAction()"

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    return true
  }

  override fun hashCode(): Int = javaClass.hashCode()

  data class RefuelActionResult(val robotFuel: Int) : Result
}
