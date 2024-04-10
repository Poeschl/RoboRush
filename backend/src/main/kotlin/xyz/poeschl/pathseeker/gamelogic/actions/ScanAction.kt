package xyz.poeschl.pathseeker.gamelogic.actions

import com.fasterxml.jackson.annotation.JsonCreator
import xyz.poeschl.pathseeker.exceptions.InsufficientFuelException
import xyz.poeschl.pathseeker.gamelogic.GameHandler
import xyz.poeschl.pathseeker.models.ActiveRobot
import xyz.poeschl.pathseeker.repositories.Tile

class ScanAction @JsonCreator constructor(val distance: Int) : RobotAction<List<Tile>> {

  override fun check(robot: ActiveRobot, gameHandler: GameHandler) {
    val scanResult = gameHandler.getTilesInDistance(robot.position, distance)
    val fuelCost = scanResult.second

    if (fuelCost > robot.fuel) {
      throw InsufficientFuelException("The available fuel (${robot.fuel}) is insufficient for the requested scan. Required fuel: $fuelCost ")
    }
  }

  override fun action(robot: ActiveRobot, gameHandler: GameHandler): List<Tile> {
    val scanResult = gameHandler.getTilesInDistance(robot.position, distance)
    val fuelCost = scanResult.second
    val tileList = scanResult.first

    robot.fuel -= fuelCost
    return tileList
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
}
