package xyz.poeschl.roborush.gamelogic

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import xyz.poeschl.roborush.controller.WebsocketController
import xyz.poeschl.roborush.exceptions.PositionNotAllowedException
import xyz.poeschl.roborush.exceptions.PositionOutOfMapException
import xyz.poeschl.roborush.gamelogic.actions.MoveAction
import xyz.poeschl.roborush.gamelogic.internal.MapHandler
import xyz.poeschl.roborush.gamelogic.internal.RobotHandler
import xyz.poeschl.roborush.models.Position
import xyz.poeschl.roborush.models.settings.BooleanSetting
import xyz.poeschl.roborush.models.settings.SettingKey
import xyz.poeschl.roborush.repositories.Tile
import xyz.poeschl.roborush.service.ConfigService
import xyz.poeschl.roborush.service.MapService
import xyz.poeschl.roborush.test.utils.builder.Builders.Companion.a
import xyz.poeschl.roborush.test.utils.builder.Builders.Companion.listWithOne
import xyz.poeschl.roborush.test.utils.builder.Builders.Companion.setWithOne
import xyz.poeschl.roborush.test.utils.builder.ConfigTypes.Companion.`$DurationSetting`
import xyz.poeschl.roborush.test.utils.builder.GameLogicBuilder.Companion.`$ActiveRobot`
import xyz.poeschl.roborush.test.utils.builder.GameLogicBuilder.Companion.`$Direction`
import xyz.poeschl.roborush.test.utils.builder.GameLogicBuilder.Companion.`$GameState`
import xyz.poeschl.roborush.test.utils.builder.GameLogicBuilder.Companion.`$Map`
import xyz.poeschl.roborush.test.utils.builder.GameLogicBuilder.Companion.`$Position`
import xyz.poeschl.roborush.test.utils.builder.GameLogicBuilder.Companion.`$Robot`
import xyz.poeschl.roborush.test.utils.builder.GameLogicBuilder.Companion.`$Tile`
import xyz.poeschl.roborush.test.utils.builder.NativeTypes.Companion.`$Boolean`
import xyz.poeschl.roborush.test.utils.builder.NativeTypes.Companion.`$Int`

class GameHandlerTest {

  private val mapHandler = mockk<MapHandler>(relaxUnitFun = true)
  private val robotHandler = mockk<RobotHandler>(relaxUnitFun = true)
  private val websocketController = mockk<WebsocketController>(relaxUnitFun = true)
  private val gameStateMachine = mockk<GameStateMachine>(relaxUnitFun = true)
  private val configService = mockk<ConfigService>(relaxUnitFun = true)
  private val mapService = mockk<MapService>(relaxUnitFun = true)

  private val gameHandler = GameHandler(mapHandler, robotHandler, websocketController, gameStateMachine, configService, mapService)

  @Test
  fun getCurrentMap() {
    // WHEN
    val tiles = listWithOne(`$Tile`())
    val map = a(`$Map`())
    tiles.forEach { map.addTile(it) }

    every { mapHandler.getCurrentMap() } returns map

    // THEN
    val result = gameHandler.getCurrentMap()

    // VERIFY
    assertThat(result).isEqualTo(map)
    assertThat(result.mapData).containsAll(tiles)
  }

  @Test
  fun isPositionValidForMove() {
    // WHEN
    val position = Position(3, 4)
    every { mapHandler.isPositionValid(position) } returns true
    every { robotHandler.isPositionFreeAfterActions(position) } returns true

    // THEN
    gameHandler.checkIfPositionIsValidForMove(position)

    // VERIFY
    // No exception, the check is successful
  }

  @Test
  fun isPositionValidForMove_invalidPosition() {
    // WHEN
    val position = Position(3, 4)
    every { mapHandler.isPositionValid(position) } returns false
    every { robotHandler.isPositionFreeAfterActions(position) } returns true

    // THEN
    assertThrows<PositionOutOfMapException> {
      gameHandler.checkIfPositionIsValidForMove(position)
    }

    // VERIFY
  }

  @Test
  fun isPositionValidForMove_positionOccupied() {
    // WHEN
    val position = Position(3, 4)
    every { mapHandler.isPositionValid(position) } returns true
    every { robotHandler.isPositionFreeAfterActions(position) } returns false

    // THEN
    assertThrows<PositionNotAllowedException> {
      gameHandler.checkIfPositionIsValidForMove(position)
    }

    // VERIFY
  }

  @Test
  fun getTilesInDistance() {
    // WHEN
    val tiles = listOf(Tile(null, Position(0, 1), 1))
    val position = Position(1, 2)
    val distance = 3
    every { mapHandler.getTilesInDistance(position, distance) } returns Pair(tiles, 23)

    // THEN
    val result = gameHandler.getTilesInDistance(position, distance)

    // VERIFY
    assertThat(result.first).isEqualTo(tiles)
    assertThat(result.second).isEqualTo(23)
  }

  @Test
  fun sendRobotUpdate() {
    // WHEN
    val robot = a(`$ActiveRobot`())

    // THEN
    gameHandler.sendRobotUpdate(robot)

    // VERIFY
    verify { websocketController.sendRobotUpdate(robot) }
  }

  @Test
  fun getFuelCostForMove() {
    // WHEN
    val fuel = 42
    val currentPos = Position(0, 0)
    val targetPos = Position(1, 0)
    every { mapHandler.getFuelCost(currentPos, targetPos) } returns fuel

    // THEN
    val result = gameHandler.getFuelCostForMove(currentPos, targetPos)

    // VERIFY
    assertThat(result).isEqualTo(fuel)
  }

  @Test
  fun getActiveRobots() {
    // WHEN
    val robots = setWithOne(`$ActiveRobot`())
    every { robotHandler.getAllActiveRobots() } returns robots

    // THEN
    val result = gameHandler.getActiveRobots()

    // VERIFY
    assertThat(result).isEqualTo(robots)
  }

  @Test
  fun getActiveRobot() {
    // WHEN
    val robot = a(`$ActiveRobot`())
    every { robotHandler.getActiveRobot(1) } returns robot

    // THEN
    val result = gameHandler.getActiveRobot(1)

    // VERIFY
    assertThat(result).isEqualTo(robot)
  }

  @Test
  fun nextActionForRobot() {
    // WHEN
    val robotId = 1L
    val action = MoveAction(a(`$Direction`()))
    val activeRobot = a(`$ActiveRobot`())
    every { robotHandler.setNextMove(robotId, gameHandler, action) } returns activeRobot

    // THEN
    gameHandler.nextActionForRobot(robotId, action)

    // VERIFY
    verify { robotHandler.setNextMove(robotId, gameHandler, action) }
    verify { websocketController.sendUserRobotData(activeRobot) }
  }

  @Test
  fun nextActionForRobot_invalidNextMove() {
    // WHEN
    val robotId = 1L
    val action = MoveAction(a(`$Direction`()))
    every { robotHandler.setNextMove(robotId, gameHandler, action) } returns null

    // THEN
    gameHandler.nextActionForRobot(robotId, action)

    // VERIFY
    verify { robotHandler.setNextMove(robotId, gameHandler, action) }
    verify(exactly = 0) { websocketController.sendUserRobotData(any()) }
  }

  @Test
  fun executeAllRobotActions() {
    // WHEN
    val knownPositions = setWithOne(`$Position`())
    val robots = setWithOne(`$ActiveRobot`().withKnownPositions(knownPositions))
    every { robotHandler.getAllActiveRobots() } returns robots

    // THEN
    gameHandler.executeAllRobotActions()

    // VERIFY
    verify { robotHandler.executeRobotActions(gameHandler) }
    verify(exactly = 1) { websocketController.sendTurnUpdate(any()) }
    verify(exactly = 1) { websocketController.sendKnownPositionsUpdate(robots.first()) }
    verify(exactly = 1) { websocketController.sendGlobalKnownPositionsUpdate(knownPositions) }
  }

  @Test
  fun registerRobotForNextGame() {
    // WHEN
    val possibleStart = listOf(Position(0, 0))
    val startPosition = Position(0, 2)
    val registeredRobot = a(`$ActiveRobot`())
    every { mapHandler.getStartPositions() } returns possibleStart
    every { robotHandler.getACurrentlyFreePosition(possibleStart) } returns startPosition
    every { robotHandler.registerRobotForGame(1, startPosition) } returns registeredRobot

    // THEN
    gameHandler.registerRobotForNextGame(1)

    // VERIFY
    verify { robotHandler.registerRobotForGame(1, startPosition) }
    verify { websocketController.sendRobotUpdate(registeredRobot) }
    verify { websocketController.sendKnownPositionsUpdate(registeredRobot) }
  }

  @Test
  fun registerRobotForNextGame_noFreeStartPosition() {
    // WHEN
    val possibleStart = listOf(Position(0, 0))
    val startPosition = Position(0, 2)
    every { mapHandler.getStartPositions() } returns possibleStart
    every { robotHandler.getACurrentlyFreePosition(possibleStart) } returns null

    // THEN
    assertThrows<PositionNotAllowedException> {
      gameHandler.registerRobotForNextGame(1)
    }

    // VERIFY
    verify(exactly = 0) { robotHandler.registerRobotForGame(1, startPosition) }
  }

  @Test
  fun prepareNewGame() {
    // WHEN
    val map = a(`$Map`())

    every { mapService.getNextChallengeMap() } returns map

    // THEN
    gameHandler.prepareNewGame()

    // VERIFY
    verify { mapHandler.loadNewMap(map) }
    verify { robotHandler.clearActiveRobots() }
    verify(exactly = 1) { websocketController.sendTurnUpdate(0) }
    verify(exactly = 1) { websocketController.sendGlobalKnownPositionsUpdate(setOf()) }
  }

  @Test
  fun getPublicGameInfo() {
    // WHEN
    val state = a(`$GameState`())
    val target = a(`$Position`())
    val chargingPossible = a(`$Boolean`())
    val robot = a(`$Robot`())

    every { gameStateMachine.getCurrentState() } returns state
    every { mapHandler.getTargetPosition() } returns target
    every { mapHandler.isSolarChargePossible() } returns chargingPossible
    every { robotHandler.getWinningRobot() } returns robot
    every { configService.getBooleanSetting(SettingKey.TARGET_POSITION_IN_GAMEINFO) } returns BooleanSetting(SettingKey.TARGET_POSITION_IN_GAMEINFO, true)
    every { configService.getDurationSetting(SettingKey.TIMEOUT_WAIT_FOR_PLAYERS) } returns a(`$DurationSetting`())
    every { configService.getDurationSetting(SettingKey.TIMEOUT_WAIT_FOR_ACTION) } returns a(`$DurationSetting`())
    every { configService.getDurationSetting(SettingKey.TIMEOUT_GAME_END) } returns a(`$DurationSetting`())

    // THEN
    val game = gameHandler.getPublicGameInfo()

    // VERIFY
    assertThat(game.currentState).isEqualTo(state)
    assertThat(game.targetPosition).isEqualTo(target)
    assertThat(game.solarChargePossible).isEqualTo(chargingPossible)
    assertThat(game.currentTurn).isEqualTo(0)
    assertThat(game.nameOfWinningRobot).isEqualTo(robot?.user!!.username)
  }

  @Test
  fun getPublicGameInfo_noTargetPosition() {
    // WHEN
    val state = a(`$GameState`())
    val target = a(`$Position`())
    val chargingPossible = a(`$Boolean`())
    val robot = a(`$Robot`())

    every { gameStateMachine.getCurrentState() } returns state
    every { mapHandler.getTargetPosition() } returns target
    every { mapHandler.isSolarChargePossible() } returns chargingPossible
    every { robotHandler.getWinningRobot() } returns robot
    every { configService.getBooleanSetting(SettingKey.TARGET_POSITION_IN_GAMEINFO) } returns BooleanSetting(SettingKey.TARGET_POSITION_IN_GAMEINFO, false)
    every { configService.getDurationSetting(SettingKey.TIMEOUT_WAIT_FOR_PLAYERS) } returns a(`$DurationSetting`())
    every { configService.getDurationSetting(SettingKey.TIMEOUT_WAIT_FOR_ACTION) } returns a(`$DurationSetting`())
    every { configService.getDurationSetting(SettingKey.TIMEOUT_GAME_END) } returns a(`$DurationSetting`())

    // THEN
    val game = gameHandler.getPublicGameInfo()

    // VERIFY
    assertThat(game.currentState).isEqualTo(state)
    assertThat(game.targetPosition).isNull()
    assertThat(game.solarChargePossible).isEqualTo(chargingPossible)
    assertThat(game.nameOfWinningRobot).isEqualTo(robot?.user!!.username)
  }

  @Test
  fun robotMovesReceived() {
    // WHEN
    val count = a(`$Int`(1, Int.MAX_VALUE))
    every { robotHandler.countPendingRobotActions() } returns count

    // THEN
    val pending = gameHandler.robotMovesReceived()

    // VERIFY
    assertThat(pending).isTrue()
  }

  @Test
  fun robotMovesReceived_none() {
    // WHEN
    every { robotHandler.countPendingRobotActions() } returns 0

    // THEN
    val pending = gameHandler.robotMovesReceived()

    // VERIFY
    assertThat(pending).isFalse()
  }

  @Test
  fun currentGameTurn_changesWithActions() {
    // WHEN
    val previousTurn = getCurrentTurn()
    val robots = setWithOne(`$ActiveRobot`())

    every { robotHandler.getAllActiveRobots() } returns robots

    // THEN
    gameHandler.executeAllRobotActions()

    // VERIFY
    val currentTurn = getCurrentTurn()
    assertThat(currentTurn).isEqualTo(previousTurn + 1)
  }

  @Test
  fun currentGameTurn_resetOnPrepare() {
    // WHEN
    val map = a(`$Map`())
    val robots = setWithOne(`$ActiveRobot`())

    every { mapService.getNextChallengeMap() } returns map
    every { robotHandler.getAllActiveRobots() } returns robots

    gameHandler.executeAllRobotActions()
    val previousTurn = getCurrentTurn()

    // THEN
    gameHandler.prepareNewGame()

    // VERIFY
    val currentTurn = getCurrentTurn()
    assertThat(previousTurn).isNotEqualTo(currentTurn)
    assertThat(currentTurn).isEqualTo(0)
  }

  private fun getCurrentTurn(): Int {
    val state = a(`$GameState`())
    val target = a(`$Position`())
    val chargingPossible = a(`$Boolean`())
    val robot = a(`$Robot`())

    every { gameStateMachine.getCurrentState() } returns state
    every { mapHandler.getTargetPosition() } returns target
    every { mapHandler.isSolarChargePossible() } returns chargingPossible
    every { robotHandler.getWinningRobot() } returns robot
    every { configService.getBooleanSetting(SettingKey.TARGET_POSITION_IN_GAMEINFO) } returns BooleanSetting(SettingKey.TARGET_POSITION_IN_GAMEINFO, false)
    every { configService.getDurationSetting(SettingKey.TIMEOUT_WAIT_FOR_PLAYERS) } returns a(`$DurationSetting`())
    every { configService.getDurationSetting(SettingKey.TIMEOUT_WAIT_FOR_ACTION) } returns a(`$DurationSetting`())
    every { configService.getDurationSetting(SettingKey.TIMEOUT_GAME_END) } returns a(`$DurationSetting`())

    return gameHandler.getPublicGameInfo().currentTurn
  }
}
