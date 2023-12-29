package xyz.poeschl.pathseeker.gamelogic.internal

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.*
import xyz.poeschl.pathseeker.controller.WebsocketController
import xyz.poeschl.pathseeker.exceptions.InsufficientFuelException
import xyz.poeschl.pathseeker.exceptions.PositionNotAllowedException
import xyz.poeschl.pathseeker.exceptions.PositionOutOfMapException
import xyz.poeschl.pathseeker.gamelogic.GameStatemachine
import xyz.poeschl.pathseeker.models.*
import xyz.poeschl.pathseeker.repositories.RobotRepository
import java.util.stream.Stream

class RobotHandlerTest {

  private val mapHandler = mock<MapHandler>()
  private val webSocketController = mock<WebsocketController>()
  private val robotRepository = mock<RobotRepository>()
  private val gameStateService = mock<GameStatemachine>()
  private val robotHandler = RobotHandler(mapHandler, webSocketController, robotRepository, gameStateService)

  companion object {
    @JvmStatic
    fun movementSource() = Stream.of(
      Arguments.of(Position(0, 0), Direction.EAST, Position(1, 0)),
      Arguments.of(Position(1, 0), Direction.WEST, Position(0, 0)),
      Arguments.of(Position(0, 0), Direction.SOUTH, Position(0, 1)),
      Arguments.of(Position(0, 1), Direction.NORTH, Position(0, 0))
    )
  }

  @ParameterizedTest
  @MethodSource("movementSource")
  fun move(oldPosition: Position, direction: Direction, expectedPosition: Position) {
    // WHEN
    val robot = ActiveRobot(1, Color(1, 2, 3), 100, oldPosition)
    `when`(mapHandler.isPositionValid(expectedPosition)).thenReturn(true)
    `when`(mapHandler.getFuelCost(oldPosition, expectedPosition)).thenReturn(10)

    // THEN
    val newPosition = robotHandler.move(robot, direction)

    // VERIFY
    assertThat(newPosition).isEqualTo(expectedPosition)
    assertThat(robot.fuel).isEqualTo(90)
    assertThat(robot.position).isEqualTo(expectedPosition)

    verify(webSocketController).sendRobotMoveUpdate(robot)
  }

  @Test
  fun move_outOfMap() {
    // WHEN
    val oldPosition = Position(0, 0)
    val expectedPosition = Position(1, 0)
    val robot = ActiveRobot(1, Color(1, 2, 3), 100, oldPosition)
    `when`(mapHandler.isPositionValid(expectedPosition)).thenReturn(false)

    // THEN
    assertThrows<PositionOutOfMapException> {
      robotHandler.move(robot, Direction.EAST)
    }

    // VERIFY
    assertThat(robot.fuel).isEqualTo(100)
    assertThat(robot.position).isEqualTo(oldPosition)

    verify(webSocketController, never()).sendRobotMoveUpdate(robot)
  }

  @Test
  fun move_positionOccupied() {
    // WHEN
    val robot1Position = Position(0, 0)
    val robot2Position = Position(1, 0)

    `when`(mapHandler.isPositionValid(robot1Position)).thenReturn(true)
    `when`(mapHandler.isPositionValid(robot2Position)).thenReturn(true)
    robotHandler.registerRobotForGame(100, robot1Position)
    robotHandler.registerRobotForGame(100, robot2Position)
    val robot1 = robotHandler.getActiveRobot(100)

    // THEN
    assertThrows<PositionNotAllowedException> {
      robotHandler.move(robot1!!, Direction.EAST)
    }

    // VERIFY
    assertThat(robot1!!.fuel).isEqualTo(100)
    assertThat(robot1.position).isEqualTo(robot1Position)

    verify(webSocketController, never()).sendRobotMoveUpdate(robot1)
  }

  @Test
  fun move_withoutFuel() {
    // WHEN
    val oldPosition = Position(0, 0)
    val expectedPosition = Position(1, 0)
    val robot = ActiveRobot(1, Color(1, 2, 3), 10, oldPosition)
    `when`(mapHandler.isPositionValid(expectedPosition)).thenReturn(true)
    `when`(mapHandler.getFuelCost(oldPosition, expectedPosition)).thenReturn(50)

    // THEN
    assertThrows<InsufficientFuelException> {
      robotHandler.move(robot, Direction.EAST)
    }

    // VERIFY
    assertThat(robot.fuel).isEqualTo(10)
    assertThat(robot.position).isEqualTo(oldPosition)

    verify(webSocketController, never()).sendRobotMoveUpdate(robot)
  }

  @Test
  fun scan() {
    // WHEN
    val robot = ActiveRobot(1, Color(1, 2, 3), 100, Position(1, 1))
    val scannedTiles = listOf(Tile(Position(1, 0), 1), Tile(Position(0, 1), 1))
    val cost = 10

    `when`(mapHandler.getTilesInDistance(Position(1, 1), 2)).thenReturn(Pair(scannedTiles, cost))

    // THEN
    val tiles = robotHandler.scan(robot, 2)

    // VERIFY
    assertThat(tiles).containsAll(scannedTiles)
    assertThat(robot.fuel).isEqualTo(90)
  }

  @Test
  fun scan_withoutFuel() {
    // WHEN
    val robot = ActiveRobot(1, Color(1, 2, 3), 10, Position(1, 1))
    val scannedTiles = listOf(Tile(Position(1, 0), 1), Tile(Position(0, 1), 1))
    val cost = 20

    `when`(mapHandler.getTilesInDistance(Position(1, 1), 2)).thenReturn(Pair(scannedTiles, cost))

    // THEN
    assertThrows<InsufficientFuelException> {
      robotHandler.scan(robot, 2)
    }

    // VERIFY
    assertThat(robot.fuel).isEqualTo(10)
  }
}
