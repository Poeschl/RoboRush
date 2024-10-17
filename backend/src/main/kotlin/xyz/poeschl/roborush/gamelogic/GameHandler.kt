package xyz.poeschl.roborush.gamelogic

import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import xyz.poeschl.roborush.configuration.GameLogic
import xyz.poeschl.roborush.controller.WebsocketController
import xyz.poeschl.roborush.exceptions.PositionNotAllowedException
import xyz.poeschl.roborush.exceptions.PositionOutOfMapException
import xyz.poeschl.roborush.gamelogic.actions.RobotAction
import xyz.poeschl.roborush.gamelogic.actions.ScanAction
import xyz.poeschl.roborush.gamelogic.internal.MapHandler
import xyz.poeschl.roborush.gamelogic.internal.RobotHandler
import xyz.poeschl.roborush.models.ActiveRobot
import xyz.poeschl.roborush.models.Game
import xyz.poeschl.roborush.models.GameTimeouts
import xyz.poeschl.roborush.models.Position
import xyz.poeschl.roborush.models.settings.SettingKey
import xyz.poeschl.roborush.repositories.Map
import xyz.poeschl.roborush.repositories.Tile
import xyz.poeschl.roborush.service.ConfigService
import xyz.poeschl.roborush.service.MapService
import xyz.poeschl.roborush.service.PlayedGamesService
import kotlin.time.measureTime

@GameLogic
class GameHandler(
  private val mapHandler: MapHandler,
  private val robotHandler: RobotHandler,
  private val websocketController: WebsocketController,
  private val gameStateMachine: GameStateMachine,
  private val configService: ConfigService,
  private val playedGamesService: PlayedGamesService,
  private val mapService: MapService
) {

  companion object {
    private val LOGGER = LoggerFactory.getLogger(GameHandler::class.java)
  }

  private var currentTurn = 0
  private var detectedIdleTurns = 0

  @Cacheable("knownMap")
  fun getCurrentMap(): Map {
    return mapHandler.getMapWithPositions(getGlobalKnownPositions())
  }

  fun getCurrentMapHeightMetadata(): Pair<Int, Int> {
    return mapHandler.getMapHeightMetaData()
  }

  fun getTileAtPosition(position: Position): Tile {
    return mapHandler.getTileAtPosition(position)
  }

  fun getTargetPosition(): Position {
    return mapHandler.getTargetPosition()
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

  fun getTilesForMovementOnPosition(position: Position): List<Tile> {
    return mapHandler.getTilesInDistance(position, configService.getIntSetting(SettingKey.DISTANCE_ROBOT_SIGHT_ON_MOVE).value).first
  }

  fun getTilesInDistance(position: Position, distance: Int): Pair<List<Tile>, Int> {
    return mapHandler.getTilesInDistance(position, distance)
  }

  fun sendRobotUpdate(activeRobot: ActiveRobot) {
    websocketController.sendRobotUpdate(activeRobot)
    websocketController.sendUserRobotData(activeRobot)
    websocketController.sendKnownPositionsUpdate(activeRobot)
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

  @CacheEvict(cacheNames = ["gameInfoCache"], allEntries = true)
  fun endingRound(winningRobot: ActiveRobot?) {
    if (winningRobot != null) {
      robotHandler.setRoundWinner(winningRobot)
    }
    playedGamesService.insertPlayedGame(winningRobot, currentTurn)
  }

  fun nextActionForRobot(robotId: Long, action: RobotAction<*>) {
    val activeRobot = robotHandler.setNextMove(robotId, this, action)
    activeRobot?.let { websocketController.sendUserRobotData(activeRobot) }
  }

  fun isGameIdle() = detectedIdleTurns >= configService.getIntSetting(SettingKey.THRESHOLD_IDLE_TURNS_FOR_ENDING_GAME).value

  fun checkForIdleRound() {
    if (robotHandler.isEveryRobotIdle()) {
      detectedIdleTurns++
    } else {
      detectedIdleTurns = 0
    }
  }

  @CacheEvict(cacheNames = ["knownMap"], allEntries = true)
  fun executeAllRobotActions() {
    robotHandler.executeRobotActions(this)
    setGameTurn(currentTurn + 1)
    websocketController.sendMapTileUpdate(getCurrentMap())
  }

  fun registerRobotForNextGame(robotId: Long) {
    val startPositions = mapHandler.getStartPositions()
    val startPosition = robotHandler.getACurrentlyFreePosition(startPositions)

    if (startPosition != null) {
      val activeRobot = robotHandler.registerRobotForGame(robotId, startPosition)
      activeRobot?.let { registeredRobot ->
        val knownTiles = getTilesForMovementOnPosition(startPosition)
        registeredRobot.knownPositions.addAll(knownTiles.map(Tile::position))
        registeredRobot.lastResult = ScanAction.ScanResult(knownTiles)
        websocketController.sendRobotUpdate(registeredRobot)
        websocketController.sendUserRobotData(registeredRobot)
        websocketController.sendKnownPositionsUpdate(registeredRobot)
        websocketController.sendMapTileUpdate(getCurrentMap())
      }
    } else {
      throw PositionNotAllowedException("Could not place robot at a empty start position.")
    }
  }

  fun prepareNewGame() {
    LOGGER.info("Select new map")
    val map: Map
    val loadDuration = measureTime {
      map = mapService.getNextChallengeMap()
      LOGGER.info("Load map {}", map.mapName)
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
      ),
      nameOfWinningRobot = robotHandler.getWinningRobot()?.user?.username,
      mapSize = mapHandler.getCurrentFullMap().size,
      fullMapScanPossible = configService.getBooleanSetting(SettingKey.ENABLE_FULL_MAP_SCAN).value
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

  fun isFullMapScanPossible() = configService.getBooleanSetting(SettingKey.ENABLE_FULL_MAP_SCAN).value

  fun getKnownPositionsForRobot(robotId: Long): Set<Position>? {
    return robotHandler.getActiveRobot(robotId)?.knownPositions
  }

  fun getGlobalKnownPositions(): Set<Position> {
    val positions = robotHandler.getAllActiveRobots().map { it.knownPositions }.flatten().toMutableSet()

    if (configService.getBooleanSetting(SettingKey.TARGET_POSITION_IN_GAMEINFO).value) {
      positions.add(mapHandler.getTargetPosition())
    }

    return positions
  }
}
