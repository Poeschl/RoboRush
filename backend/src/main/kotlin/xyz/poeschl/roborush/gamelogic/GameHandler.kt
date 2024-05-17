package xyz.poeschl.roborush.gamelogic

import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import xyz.poeschl.roborush.configuration.GameLogic
import xyz.poeschl.roborush.controller.WebsocketController
import xyz.poeschl.roborush.exceptions.PositionNotAllowedException
import xyz.poeschl.roborush.exceptions.PositionOutOfMapException
import xyz.poeschl.roborush.gamelogic.actions.RobotAction
import xyz.poeschl.roborush.gamelogic.internal.MapHandler
import xyz.poeschl.roborush.gamelogic.internal.RobotHandler
import xyz.poeschl.roborush.models.*
import xyz.poeschl.roborush.models.settings.SettingKey
import xyz.poeschl.roborush.repositories.Map
import xyz.poeschl.roborush.repositories.Tile
import xyz.poeschl.roborush.service.ConfigService
import xyz.poeschl.roborush.service.MapService
import kotlin.time.measureTime

@GameLogic
class GameHandler(
  private val mapHandler: MapHandler,
  private val robotHandler: RobotHandler,
  private val websocketController: WebsocketController,
  private val gameStateMachine: GameStateMachine,
  private val configService: ConfigService,
  private val mapService: MapService
) {

  companion object {
    private val LOGGER = LoggerFactory.getLogger(GameHandler::class.java)
  }

  private var currentTurn = 0

  fun getCurrentMap(): Map {
    return mapHandler.getCurrentMap()
  }

  fun getTileAtPosition(position: Position): Tile {
    return mapHandler.getTileAtPosition(position)
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
    setGameTurn(currentTurn + 1)
  }

  fun registerRobotForNextGame(robotId: Long) {
    val startPositions = mapHandler.getStartPositions()
    val startPosition = robotHandler.getACurrentlyFreePosition(startPositions)

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
    LOGGER.info("Load new map")
    val map: Map
    val loadDuration = measureTime {
      map = mapService.getNextChallengeMap()
      mapHandler.loadNewMap(map)
    }
    LOGGER.info("Loaded map '{}' in {} ms", map.mapName, loadDuration.inWholeMilliseconds)

    robotHandler.setRobotMaxFuel(map.maxRobotFuel)
    robotHandler.clearActiveRobots()
    setGameTurn(0)
  }

  @Cacheable("gameInfoCache")
  fun getPublicGameInfo(): Game {
    return Game(
      currentState = gameStateMachine.getCurrentState(),
      currentTurn = currentTurn,
      targetPosition = if (configService.getBooleanSetting(SettingKey.TARGET_POSITION_IN_GAMEINFO).value) mapHandler.getTargetPosition() else null,
      solarChargePossible = mapHandler.isSolarChargePossible(),
      gameTimeoutsInMillis = GameTimeouts(
        waitForPlayers = configService.getDurationSetting(SettingKey.TIMEOUT_WAIT_FOR_PLAYERS).value.inWholeMilliseconds,
        waitForAction = configService.getDurationSetting(SettingKey.TIMEOUT_WAIT_FOR_ACTION).value.inWholeMilliseconds,
        gameEnd = configService.getDurationSetting(SettingKey.TIMEOUT_GAME_END).value.inWholeMilliseconds
      )
    )
  }

  fun getRobotMaxFuel(): Int {
    return mapHandler.getRobotMaxFuel()
  }

  fun isSolarChargePossible(): Boolean {
    return mapHandler.isSolarChargePossible()
  }

  fun getSolarChargeRate(): Double {
    return mapHandler.getSolarChargeRate()
  }

  private fun setGameTurn(turn: Int) {
    currentTurn = turn
    websocketController.sendTurnUpdate(currentTurn)
  }
}
