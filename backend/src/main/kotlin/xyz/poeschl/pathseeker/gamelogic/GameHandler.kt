package xyz.poeschl.pathseeker.gamelogic

import xyz.poeschl.pathseeker.configuration.GameLogic
import xyz.poeschl.pathseeker.controller.WebsocketController
import xyz.poeschl.pathseeker.exceptions.PositionNotAllowedException
import xyz.poeschl.pathseeker.exceptions.PositionOutOfMapException
import xyz.poeschl.pathseeker.gamelogic.actions.RobotAction
import xyz.poeschl.pathseeker.gamelogic.internal.MapHandler
import xyz.poeschl.pathseeker.gamelogic.internal.RobotHandler
import xyz.poeschl.pathseeker.models.ActiveRobot
import xyz.poeschl.pathseeker.models.Position
import xyz.poeschl.pathseeker.models.Size
import xyz.poeschl.pathseeker.models.Tile

@GameLogic
class GameHandler(
  private val mapHandler: MapHandler,
  private val robotHandler: RobotHandler,
  private val websocketController: WebsocketController
) {

  fun getHeightMap(): List<Tile> {
    return mapHandler.getHeightMap()
  }

  /**
   * Will check the position for a valid movement.
   * For an incorrect movement exceptions are thrown.
   *
   * @see PositionNotAllowedException
   * @see PositionOutOfMapException
   */
  fun isPositionValidForMove(position: Position) {
    if (!mapHandler.isPositionValid(position)) {
      throw PositionOutOfMapException("Position $position is not in map bounds.")
    }
    if (robotHandler.isPositionOccupied(position)) {
      throw PositionNotAllowedException("Position $position is already occupied.")
    }
  }

  fun getTilesInDistance(position: Position, scanDistance: Int): Pair<List<Tile>, Int> {
    return mapHandler.getTilesInDistance(position, scanDistance)
  }

  fun sendRobotUpdate(activeRobot: ActiveRobot) {
    websocketController.sendRobotUpdate(activeRobot)
  }

  fun getFuelCostForMove(current: Position, next: Position): Int {
    return mapHandler.getFuelCost(current, next)
  }

  fun getActiveRobots(): List<ActiveRobot> {
    return robotHandler.getAllActiveRobots()
  }

  fun getActiveRobot(robotId: Long): ActiveRobot? {
    return robotHandler.getActiveRobot(robotId)
  }

  fun nextActionForRobot(robotId: Long, action: RobotAction<*>) {
    return robotHandler.setNextMove(robotId, this, action)
  }

  fun executeAllRobotMoves() {
    robotHandler.executeRobotMoves(this)
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

  fun prepareNewGame() {
    mapHandler.createNewRandomMap(Size(16, 8))
    robotHandler.clearActiveRobots()
  }
}
