package xyz.poeschl.pathseeker.gamelogic

import xyz.poeschl.pathseeker.configuration.GameLogic
import xyz.poeschl.pathseeker.controller.WebsocketController
import xyz.poeschl.pathseeker.exceptions.PositionNotAllowedException
import xyz.poeschl.pathseeker.exceptions.PositionOutOfMapException
import xyz.poeschl.pathseeker.gamelogic.actions.RobotAction
import xyz.poeschl.pathseeker.gamelogic.internal.MapHandler
import xyz.poeschl.pathseeker.gamelogic.internal.RobotHandler
import xyz.poeschl.pathseeker.models.*

@GameLogic
class GameHandler(
  private val mapHandler: MapHandler,
  private val robotHandler: RobotHandler,
  private val websocketController: WebsocketController,
  private val gameStateMachine: GameStateMachine
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
  fun checkIfPositionIsValidForMove(position: Position) {
    if (!mapHandler.isPositionValid(position)) {
      throw PositionOutOfMapException("Position $position is not in map bounds.")
    }
    if (!robotHandler.isPositionFreeAfterActions(position)) {
      throw PositionNotAllowedException("Position $position is already occupied.")
    }
  }

  fun getTilesInDistance(position: Position, distance: Int): Pair<List<Tile>, Int> {
    return mapHandler.getTilesInDistance(position, distance)
  }

  fun sendRobotUpdate(activeRobot: ActiveRobot) {
    websocketController.sendRobotUpdate(activeRobot)
    websocketController.sendUserRobotData(activeRobot)
  }

  fun getFuelCostForMove(current: Position, next: Position): Int {
    return mapHandler.getFuelCost(current, next)
  }

  fun getActiveRobots(): Set<ActiveRobot> {
    return robotHandler.getAllActiveRobots()
  }

  fun getActiveRobot(robotId: Long): ActiveRobot? {
    return robotHandler.getActiveRobot(robotId)
  }

  fun nextActionForRobot(robotId: Long, action: RobotAction<*>) {
    val activeRobot = robotHandler.setNextMove(robotId, this, action)
    activeRobot?.let { websocketController.sendUserRobotData(activeRobot) }
  }

  fun robotMovesReceived(): Boolean = robotHandler.countPendingRobotActions() > 0

  fun executeAllRobotMoves() {
    robotHandler.executeRobotActions(this)
  }

  fun registerRobotForNextGame(robotId: Long) {
    val startPositions = mapHandler.getStartPositions()
    val startPosition = robotHandler.getFirstCurrentlyFreePosition(startPositions)

    if (startPosition != null) {
      val activeRobot = robotHandler.registerRobotForGame(robotId, startPosition)
      activeRobot?.let {
        websocketController.sendRobotUpdate(activeRobot)
        websocketController.sendUserRobotData(activeRobot)
      }
    } else {
      throw PositionNotAllowedException("Could not place robot at a empty start position.")
    }
  }

  fun prepareNewGame() {
    mapHandler.createNewRandomMap(Size(16, 8))
    robotHandler.clearActiveRobots()
  }

  fun getPublicGameInfo(): Game {
    return Game(gameStateMachine.getCurrentState())
  }
}
