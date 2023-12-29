package xyz.poeschl.pathseeker.gamelogic

import xyz.poeschl.pathseeker.configuration.GameLogic
import xyz.poeschl.pathseeker.exceptions.PositionNotAllowedException
import xyz.poeschl.pathseeker.gamelogic.actions.RobotAction
import xyz.poeschl.pathseeker.gamelogic.internal.MapHandler
import xyz.poeschl.pathseeker.gamelogic.internal.RobotHandler
import xyz.poeschl.pathseeker.models.ActiveRobot
import xyz.poeschl.pathseeker.models.Tile

@GameLogic
class GameHandler(private val mapHandler: MapHandler, private val robotHandler: RobotHandler) {

  fun getHeightMap(): List<Tile> {
    return mapHandler.getHeightMap()
  }

  fun getActiveRobots(): List<ActiveRobot> {
    return robotHandler.getAllActiveRobots()
  }

  fun getActiveRobot(robotId: Long): ActiveRobot? {
    return robotHandler.getActiveRobot(robotId)
  }

  fun nextActionForRobot(robotId: Long, action: RobotAction) {
    return robotHandler.setNextMove(robotId, action)
  }

  fun registerRobotForNextGame(robotId: Long) {
    val startPositions = mapHandler.getStartPositions()
    val startPosition = robotHandler.getFirstNotOccupiedPosition(startPositions)

    if (startPosition != null) {
      robotHandler.registerRobotForGame(robotId, startPosition)
    } else {
      throw PositionNotAllowedException("Could not place robot at a empty start position.")
    }
  }
}
