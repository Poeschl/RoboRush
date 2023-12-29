package xyz.poeschl.pathseeker.gamelogic.actions

import xyz.poeschl.pathseeker.exceptions.InsufficientFuelException
import xyz.poeschl.pathseeker.gamelogic.GameHandler
import xyz.poeschl.pathseeker.models.ActiveRobot
import xyz.poeschl.pathseeker.models.Tile

class ScanAction(private val scanDistance: Int) : RobotAction<List<Tile>> {

  override fun check(robot: ActiveRobot, gameHandler: GameHandler) {
    val scanResult = gameHandler.getTilesInDistance(robot.position, scanDistance)
    val fuelCost = scanResult.second

    if (fuelCost > robot.fuel) {
      throw InsufficientFuelException("The available fuel (${robot.fuel}) is insufficient for the requested scan. Required fuel: $fuelCost ")
    }
  }

  override fun action(robot: ActiveRobot, gameHandler: GameHandler): List<Tile> {
    val scanResult = gameHandler.getTilesInDistance(robot.position, scanDistance)
    val fuelCost = scanResult.second
    val tileList = scanResult.first

    robot.fuel -= fuelCost
    return tileList
  }

  override fun toString(): String {
    return "Scan(scanDistance=$scanDistance)"
  }
}
