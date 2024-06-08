package xyz.poeschl.roborush.gamelogic.actions

import com.fasterxml.jackson.annotation.JsonCreator
import xyz.poeschl.roborush.exceptions.InsufficientFuelException
import xyz.poeschl.roborush.gamelogic.GameHandler
import xyz.poeschl.roborush.models.ActiveRobot
import xyz.poeschl.roborush.repositories.Tile

class ScanAction @JsonCreator constructor(val distance: Int) : RobotAction<ScanAction.ScanResult> {

  override fun check(robot: ActiveRobot, gameHandler: GameHandler) {
    val scanResult = gameHandler.getTilesInDistance(robot.position, distance)
    val fuelCost = scanResult.second

    if (fuelCost > robot.fuel) {
      throw InsufficientFuelException("The available fuel (${robot.fuel}) is insufficient for the requested scan. Required fuel: $fuelCost ")
    }
  }

  override fun action(robot: ActiveRobot, gameHandler: GameHandler): ScanResult {
    val scanResult = gameHandler.getTilesInDistance(robot.position, distance)
    val fuelCost = scanResult.second
    val tileList = scanResult.first

    robot.fuel -= fuelCost
    robot.knownPositions.addAll(tileList.map { it.position })
    gameHandler.sendRobotUpdate(robot)
    return ScanResult(tileList)
  }

  override fun toString(): String {
    return "Scan(distance=$distance)"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as ScanAction

    return distance == other.distance
  }

  override fun hashCode(): Int {
    return distance
  }

  data class ScanResult(val tiles: List<Tile>)
}
