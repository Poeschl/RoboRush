package xyz.poeschl.pathseeker.gamelogic

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import xyz.poeschl.pathseeker.controller.WebsocketController
import xyz.poeschl.pathseeker.exceptions.PositionNotAllowedException
import xyz.poeschl.pathseeker.exceptions.PositionOutOfMapException
import xyz.poeschl.pathseeker.gamelogic.actions.MoveAction
import xyz.poeschl.pathseeker.gamelogic.internal.MapHandler
import xyz.poeschl.pathseeker.gamelogic.internal.RobotHandler
import xyz.poeschl.pathseeker.models.*
import xyz.poeschl.pathseeker.test.utils.builder.Builders.Companion.a
import xyz.poeschl.pathseeker.test.utils.builder.Builders.Companion.listWithOne
import xyz.poeschl.pathseeker.test.utils.builder.Builders.Companion.setWithOne
import xyz.poeschl.pathseeker.test.utils.builder.GameLogicBuilder.Companion.`$ActiveRobot`
import xyz.poeschl.pathseeker.test.utils.builder.GameLogicBuilder.Companion.`$Direction`
import xyz.poeschl.pathseeker.test.utils.builder.GameLogicBuilder.Companion.`$GameState`
import xyz.poeschl.pathseeker.test.utils.builder.GameLogicBuilder.Companion.`$Position`
import xyz.poeschl.pathseeker.test.utils.builder.GameLogicBuilder.Companion.`$Tile`
import xyz.poeschl.pathseeker.test.utils.builder.NativeTypes.Companion.`$Int`

class GameHandlerTest {

  private val mapHandler = mockk<MapHandler>(relaxUnitFun = true)
  private val robotHandler = mockk<RobotHandler>(relaxUnitFun = true)
  private val websocketController = mockk<WebsocketController>(relaxUnitFun = true)
  private val gameStateMachine = mockk<GameStateMachine>(relaxUnitFun = true)

  private val gameHandler = GameHandler(mapHandler, robotHandler, websocketController, gameStateMachine)

  @Test
  fun getHeightMap() {
    // WHEN
    val tiles = listWithOne(`$Tile`())
    every { mapHandler.getHeightMap() } returns tiles

    // THEN
    val result = gameHandler.getHeightMap()

    // VERIFY
    assertThat(result).isEqualTo(tiles)
  }

  @Test
  fun getTargetPosition() {
    // WHEN

    // THEN

    // VERIFY
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
    val tiles = listOf(Tile(Position(0, 1), 1))
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
  fun executeAllRobotMoves() {
    // WHEN

    // THEN
    gameHandler.executeAllRobotMoves()

    // VERIFY
    verify { robotHandler.executeRobotActions(gameHandler) }
  }

  @Test
  fun registerRobotForNextGame() {
    // WHEN
    val possibleStart = listOf(Position(0, 0))
    val startPosition = Position(0, 2)
    val registeredRobot = a(`$ActiveRobot`())
    every { mapHandler.getStartPositions() } returns possibleStart
    every { robotHandler.getFirstCurrentlyFreePosition(possibleStart) } returns startPosition
    every { robotHandler.registerRobotForGame(1, startPosition) } returns registeredRobot

    // THEN
    gameHandler.registerRobotForNextGame(1)

    // VERIFY
    verify { robotHandler.registerRobotForGame(1, startPosition) }
    verify { websocketController.sendRobotUpdate(registeredRobot) }
  }

  @Test
  fun registerRobotForNextGame_noFreeStartPosition() {
    // WHEN
    val possibleStart = listOf(Position(0, 0))
    val startPosition = Position(0, 2)
    every { mapHandler.getStartPositions() } returns possibleStart
    every { robotHandler.getFirstCurrentlyFreePosition(possibleStart) } returns null

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

    // THEN
    gameHandler.prepareNewGame()

    // VERIFY
    verify { mapHandler.createNewRandomMap(Size(16, 8)) }
    verify { robotHandler.clearActiveRobots() }
  }

  @Test
  fun getPublicGameInfo() {
    // WHEN
    val state = a(`$GameState`())
    val target = a(`$Position`())
    every { gameStateMachine.getCurrentState() } returns state
    every { mapHandler.getTargetPosition() } returns target

    // THEN
    val game = gameHandler.getPublicGameInfo()

    // VERIFY
    assertThat(game.currentState).isEqualTo(state)
    assertThat(game.targetPosition).isEqualTo(target)
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
}
