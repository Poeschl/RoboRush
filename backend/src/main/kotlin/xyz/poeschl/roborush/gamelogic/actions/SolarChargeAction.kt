package xyz.poeschl.roborush.gamelogic.actions

import com.fasterxml.jackson.annotation.JsonCreator
import org.slf4j.LoggerFactory
import xyz.poeschl.roborush.exceptions.ActionDeniedByConfig
import xyz.poeschl.roborush.gamelogic.GameHandler
import xyz.poeschl.roborush.models.ActiveRobot
import kotlin.math.floor

class SolarChargeAction @JsonCreator constructor() : RobotAction<Int> {

  companion object {
    val LOGGER = LoggerFactory.getLogger(SolarChargeAction::class.java)
  }

  override fun check(robot: ActiveRobot, gameHandler: GameHandler) {
    if (!gameHandler.isSolarChargePossible()) {
      throw ActionDeniedByConfig("Solar charging is not possible on this map.")
    }
  }

  override fun action(robot: ActiveRobot, gameHandler: GameHandler): Int {
    val addedFuel = floor(gameHandler.getRobotMaxFuel() * gameHandler.getSolarChargeRate()).toInt()
    robot.fuel = (robot.fuel + addedFuel).coerceAtMost(robot.maxFuel)
    LOGGER.debug("Solar charge robot {} to {}", robot.id, robot.fuel)

    return robot.fuel
  }

  override fun toString(): String {
    return "SolarChargeAction()"
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
