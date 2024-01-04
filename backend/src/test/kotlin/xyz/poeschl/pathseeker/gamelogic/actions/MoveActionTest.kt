package xyz.poeschl.pathseeker.gamelogic.actions

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.*
import xyz.poeschl.pathseeker.exceptions.InsufficientFuelException
import xyz.poeschl.pathseeker.exceptions.PositionNotAllowedException
import xyz.poeschl.pathseeker.exceptions.PositionOutOfMapException
import xyz.poeschl.pathseeker.gamelogic.GameHandler
import xyz.poeschl.pathseeker.models.*
import java.util.stream.Stream

class MoveActionTest {

  private val gameHandler = mock<GameHandler>()

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
  fun moveCheck(oldPosition: Position, direction: Direction, expectedPosition: Position) {
    // WHEN
    val robot = ActiveRobot(1, Color(1, 2, 3), 100, oldPosition)
    `when`(gameHandler.getFuelCostForMove(oldPosition, expectedPosition)).thenReturn(10)
    val action = MoveAction(direction)

    // THEN no exception is thrown
    action.check(robot, gameHandler)

    // VERIFY
  }

  @Test
  fun moveCheck_outOfMap() {
    // WHEN
    val oldPosition = Position(0, 0)
    val expectedPosition = Position(1, 0)
    val robot = ActiveRobot(1, Color(1, 2, 3), 100, oldPosition)
    `when`(gameHandler.checkIfPositionIsValidForMove(expectedPosition)).thenThrow(PositionOutOfMapException(""))
    val action = MoveAction(Direction.EAST)

    // THEN
    assertThrows<PositionOutOfMapException> {
      action.check(robot, gameHandler)
    }

    // VERIFY
  }

  @Test
  fun moveCheck_positionOccupied() {
    // WHEN
    val oldPosition = Position(0, 0)
    val robot = ActiveRobot(1, Color(1, 2, 3), 100, oldPosition)

    `when`(gameHandler.checkIfPositionIsValidForMove(Position(1, 0))).thenThrow(PositionNotAllowedException(""))
    val action = MoveAction(Direction.EAST)

    // THEN
    assertThrows<PositionNotAllowedException> {
      action.check(robot, gameHandler)
    }

    // VERIFY
  }

  @Test
  fun moveCheck_withoutFuel() {
    // WHEN
    val oldPosition = Position(0, 0)
    val robot = ActiveRobot(1, Color(1, 2, 3), 100, oldPosition)

    `when`(gameHandler.getFuelCostForMove(oldPosition, Position(1, 0))).thenReturn(101)
    val action = MoveAction(Direction.EAST)

    // THEN
    assertThrows<InsufficientFuelException> {
      action.check(robot, gameHandler)
    }

    // VERIFY
  }

  @ParameterizedTest
  @MethodSource("movementSource")
  fun moveAction(oldPosition: Position, direction: Direction, expectedPosition: Position) {
    // WHEN
    val robot = ActiveRobot(1, Color(1, 2, 3), 100, oldPosition)
    `when`(gameHandler.getFuelCostForMove(oldPosition, expectedPosition)).thenReturn(10)
    val action = MoveAction(direction)

    // THEN
    val newPosition = action.action(robot, gameHandler)

    // VERIFY
    assertThat(newPosition).isEqualTo(expectedPosition)
    assertThat(robot.fuel).isEqualTo(90)
    assertThat(robot.position).isEqualTo(expectedPosition)

    verify(gameHandler).sendRobotUpdate(robot)
  }
}
