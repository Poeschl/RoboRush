package xyz.poeschl.roborush.gamelogic.actions

import com.fasterxml.jackson.annotation.JsonCreator
import xyz.poeschl.roborush.exceptions.ActionDeniedByConfig
import xyz.poeschl.roborush.gamelogic.GameHandler
import xyz.poeschl.roborush.gamelogic.actions.ScanAction.ScanResult
import xyz.poeschl.roborush.gamelogic.actions.SolarChargeAction.Companion.LOGGER
import xyz.poeschl.roborush.models.ActiveRobot

class FullMapScanAction @JsonCreator constructor() : RobotAction<ScanResult> {

  override fun check(robot: ActiveRobot, gameHandler: GameHandler) {
    val isAllowed = gameHandler.isFullMapScanPossible()

    if (!isAllowed) {
      throw ActionDeniedByConfig("The full map scan is currently not allowed!")
    }
  }

  override fun action(robot: ActiveRobot, gameHandler: GameHandler): RobotActionResult<ScanResult> {
    val tileList = gameHandler.getCurrentMap().mapData

    robot.knownPositions.addAll(tileList.map { it.position })
    LOGGER.debug("Robot {} did a full scan", robot.id)
    gameHandler.sendRobotUpdate(robot)
    return RobotActionResult(robot, ScanResult(tileList))
  }

  override fun toString(): String {
    return "FullScan()"
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
