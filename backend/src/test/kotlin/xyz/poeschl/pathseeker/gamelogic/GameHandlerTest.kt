package xyz.poeschl.pathseeker.gamelogic

import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import xyz.poeschl.pathseeker.controller.WebsocketController
import xyz.poeschl.pathseeker.exceptions.PositionNotAllowedException
import xyz.poeschl.pathseeker.exceptions.PositionOutOfMapException
import xyz.poeschl.pathseeker.gamelogic.actions.MoveAction
import xyz.poeschl.pathseeker.gamelogic.internal.MapHandler
import xyz.poeschl.pathseeker.gamelogic.internal.RobotHandler
import xyz.poeschl.pathseeker.models.*

class GameHandlerTest {

  private val mapHandler = mock<MapHandler>()
  private val robotHandler = mock<RobotHandler>()
  private val websocketController = mock<WebsocketController>()

  private val gameHandler = GameHandler(mapHandler, robotHandler, websocketController)

  @Test
  fun getHeightMap() {
    // WHEN
    val tiles = listOf(Tile(Position(0, 1), 1))
    `when`(mapHandler.getHeightMap()).thenReturn(tiles)

    // THEN
    val result = gameHandler.getHeightMap()

    // VERIFY
    assertThat(result).isEqualTo(tiles)
  }

  @Test
  fun isPositionValidForMove() {
    // WHEN
    val position = Position(3, 4)
    `when`(mapHandler.isPositionValid(position)).thenReturn(true)
    `when`(robotHandler.isPositionFreeAfterActions(position)).thenReturn(true)

    // THEN
    gameHandler.checkIfPositionIsValidForMove(position)

    // VERIFY
    // No exception, the check is successful
  }

  @Test
  fun isPositionValidForMove_invalidPosition() {
    // WHEN
    val position = Position(3, 4)
    `when`(mapHandler.isPositionValid(position)).thenReturn(false)
    `when`(robotHandler.isPositionFreeAfterActions(position)).thenReturn(true)

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
    `when`(mapHandler.isPositionValid(position)).thenReturn(true)
    `when`(robotHandler.isPositionFreeAfterActions(position)).thenReturn(false)

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
    `when`(mapHandler.getTilesInDistance(position, distance)).thenReturn(Pair(tiles, 23))

    // THEN
    val result = gameHandler.getTilesInDistance(position, distance)

    // VERIFY
    assertThat(result.first).isEqualTo(tiles)
    assertThat(result.second).isEqualTo(23)
  }

  @Test
  fun sendRobotUpdate() {
    // WHEN
    val robot = ActiveRobot(1, Color.randomColor(), 100, Position(0, 0))

    // THEN
    gameHandler.sendRobotUpdate(robot)

    // VERIFY
    verify(websocketController).sendRobotUpdate(robot)
  }

  @Test
  fun getFuelCostForMove() {
    // WHEN
    val fuel = 42
    val currentPos = Position(0, 0)
    val targetPos = Position(1, 0)
    `when`(mapHandler.getFuelCost(currentPos, targetPos)).thenReturn(fuel)

    // THEN
    val result = gameHandler.getFuelCostForMove(currentPos, targetPos)

    // VERIFY
    assertThat(result).isEqualTo(fuel)
  }

  @Test
  fun getActiveRobots() {
    // WHEN
    val robots = setOf(ActiveRobot(1, Color.randomColor(), 100, Position(0, 0)))
    `when`(robotHandler.getAllActiveRobots()).thenReturn(robots)

    // THEN
    val result = gameHandler.getActiveRobots()

    // VERIFY
    assertThat(result).isEqualTo(robots)
  }

  @Test
  fun getActiveRobot() {
    // WHEN
    val robot = ActiveRobot(1, Color.randomColor(), 100, Position(0, 0))
    `when`(robotHandler.getActiveRobot(1)).thenReturn(robot)

    // THEN
    val result = gameHandler.getActiveRobot(1)

    // VERIFY
    assertThat(result).isEqualTo(robot)
  }

  @Test
  fun nextActionForRobot() {
    // WHEN
    val robotId = 1L
    val action = MoveAction(Direction.NORTH)

    // THEN
    gameHandler.nextActionForRobot(robotId, action)

    // VERIFY
    verify(robotHandler).setNextMove(robotId, gameHandler, action)
  }

  @Test
  fun executeAllRobotMoves() {
    // WHEN

    // THEN
    gameHandler.executeAllRobotMoves()

    // VERIFY
    verify(robotHandler).executeRobotActions(gameHandler)
  }

  @Test
  fun registerRobotForNextGame() {
    // WHEN
    val possibleStart = listOf(Position(0, 0))
    val startPosition = Position(0, 2)
    `when`(mapHandler.getStartPositions()).thenReturn(possibleStart)
    `when`(robotHandler.getFirstCurrentlyFreePosition(possibleStart)).thenReturn(startPosition)

    // THEN
    gameHandler.registerRobotForNextGame(1)

    // VERIFY
    verify(robotHandler).registerRobotForGame(1, startPosition)
  }

  @Test
  fun registerRobotForNextGame_noFreeStartPosition() {
    // WHEN
    val possibleStart = listOf(Position(0, 0))
    val startPosition = Position(0, 2)
    `when`(mapHandler.getStartPositions()).thenReturn(possibleStart)
    `when`(robotHandler.getFirstCurrentlyFreePosition(possibleStart)).thenReturn(null)

    // THEN
    assertThrows<PositionNotAllowedException> {
      gameHandler.registerRobotForNextGame(1)
    }

    // VERIFY
    verify(robotHandler, never()).registerRobotForGame(1, startPosition)
  }

  @Test
  fun prepareNewGame() {
    // WHEN

    // THEN
    gameHandler.prepareNewGame()

    // VERIFY
    verify(mapHandler).createNewRandomMap(Size(16, 8))
    verify(robotHandler).clearActiveRobots()
  }
}
