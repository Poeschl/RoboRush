package xyz.poeschl.roborush.gamelogic.actions

import com.fasterxml.jackson.annotation.JsonCreator
import org.slf4j.LoggerFactory
import xyz.poeschl.roborush.exceptions.ActionDeniedByConfig
import xyz.poeschl.roborush.exceptions.TankFullException
import xyz.poeschl.roborush.gamelogic.GameHandler
import xyz.poeschl.roborush.gamelogic.actions.RefuelAction.RefuelActionResult
import xyz.poeschl.roborush.models.ActiveRobot
import kotlin.math.ceil

class SolarChargeAction @JsonCreator constructor() : RobotAction<RefuelActionResult> {

  companion object {
    val LOGGER = LoggerFactory.getLogger(SolarChargeAction::class.java)
  }

  override fun check(robot: ActiveRobot, gameHandler: GameHandler) {
    if (!gameHandler.isSolarChargePossible()) {
      throw ActionDeniedByConfig("Solar charging is not possible on this map.")
    }

    if (!robot.canRetrieveFuel()) {
      throw TankFullException("Fuel tank is already full")
    }
  }

  override fun action(robot: ActiveRobot, gameHandler: GameHandler): RobotActionResult<RefuelActionResult> {
    val addedFuel = ceil(gameHandler.getRobotMaxFuel() * gameHandler.getSolarChargeRate()).toInt()
    robot.addFuel(addedFuel)
    LOGGER.debug("Solar charge robot {} to {}", robot.id, robot.fuel)

    return RobotActionResult(robot, RefuelActionResult(robot.fuel))
  }

  override fun toString(): String = "SolarChargeAction()"

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    return true
  }

  override fun hashCode(): Int = javaClass.hashCode()
}
