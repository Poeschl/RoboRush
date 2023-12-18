package xyz.poeschl.pathseeker.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.*
import xyz.poeschl.pathseeker.controller.WebsocketController
import xyz.poeschl.pathseeker.exceptions.InsufficientFuelException
import xyz.poeschl.pathseeker.exceptions.MoveOutOfMapException
import xyz.poeschl.pathseeker.models.*
import java.util.stream.Stream

class RobotServiceTest {

  private val mapService = mock<MapService>()
  private val webSocketController = mock<WebsocketController>()
  private val robotService = RobotService(mapService, webSocketController)

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
    val robot = Robot(1, Color(1, 2, 3), 100, oldPosition)
    `when`(mapService.isPositionValid(expectedPosition)).thenReturn(true)
    `when`(mapService.getFuelCost(oldPosition, expectedPosition)).thenReturn(10)

    // THEN
    val newPosition = robotService.move(robot, direction)

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
    val robot = Robot(1, Color(1, 2, 3), 100, oldPosition)
    `when`(mapService.isPositionValid(expectedPosition)).thenReturn(false)

    // THEN
    assertThrows<MoveOutOfMapException> {
      robotService.move(robot, Direction.EAST)
    }

    // VERIFY
    assertThat(robot.fuel).isEqualTo(100)
    assertThat(robot.position).isEqualTo(oldPosition)

    verify(webSocketController, never()).sendRobotMoveUpdate(robot)
  }

  @Test
  fun move_withoutFuel() {
    // WHEN
    val oldPosition = Position(0, 0)
    val expectedPosition = Position(1, 0)
    val robot = Robot(1, Color(1, 2, 3), 10, oldPosition)
    `when`(mapService.isPositionValid(expectedPosition)).thenReturn(true)
    `when`(mapService.getFuelCost(oldPosition, expectedPosition)).thenReturn(50)

    // THEN
    assertThrows<InsufficientFuelException> {
      robotService.move(robot, Direction.EAST)
    }

    // VERIFY
    assertThat(robot.fuel).isEqualTo(10)
    assertThat(robot.position).isEqualTo(oldPosition)

    verify(webSocketController, never()).sendRobotMoveUpdate(robot)
  }

  @Test
  fun scan() {
    // WHEN
    val robot = Robot(1, Color(1, 2, 3), 100, Position(1, 1))
    val scannedTiles = listOf(Tile(Position(1, 0), 1), Tile(Position(0, 1), 1))
    val cost = 10

    `when`(mapService.getTilesInDistance(Position(1, 1), 2)).thenReturn(Pair(scannedTiles, cost))

    // THEN
    val tiles = robotService.scan(robot, 2)

    // VERIFY
    assertThat(tiles).containsAll(scannedTiles)
    assertThat(robot.fuel).isEqualTo(90)
  }

  @Test
  fun scan_withoutFuel() {
    // WHEN
    val robot = Robot(1, Color(1, 2, 3), 10, Position(1, 1))
    val scannedTiles = listOf(Tile(Position(1, 0), 1), Tile(Position(0, 1), 1))
    val cost = 20

    `when`(mapService.getTilesInDistance(Position(1, 1), 2)).thenReturn(Pair(scannedTiles, cost))

    // THEN
    assertThrows<InsufficientFuelException> {
      robotService.scan(robot, 2)
    }

    // VERIFY
    assertThat(robot.fuel).isEqualTo(10)
  }
}
