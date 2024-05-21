package xyz.poeschl.roborush.gamelogic.actions

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import xyz.poeschl.roborush.exceptions.InsufficientFuelException
import xyz.poeschl.roborush.exceptions.PositionNotAllowedException
import xyz.poeschl.roborush.exceptions.PositionOutOfMapException
import xyz.poeschl.roborush.gamelogic.GameHandler
import xyz.poeschl.roborush.models.Direction
import xyz.poeschl.roborush.models.Position
import xyz.poeschl.roborush.test.utils.builder.Builders.Companion.a
import xyz.poeschl.roborush.test.utils.builder.GameLogicBuilder.Companion.`$ActiveRobot`
import java.util.stream.Stream

class MoveActionTest {

  private val gameHandler = mockk<GameHandler>(relaxUnitFun = true)
  private val objectMapper = jacksonObjectMapper()

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
    val robot = a(`$ActiveRobot`().withPosition(oldPosition))
    every { gameHandler.getFuelCostForMove(oldPosition, expectedPosition) } returns 10
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
    val robot = a(`$ActiveRobot`().withPosition(oldPosition))
    every { gameHandler.checkIfPositionIsValidForMove(expectedPosition) } throws PositionOutOfMapException("")
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
    val robot = a(`$ActiveRobot`().withPosition(oldPosition))

    every { gameHandler.checkIfPositionIsValidForMove(Position(1, 0)) } throws PositionNotAllowedException("")
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
    val robot = a(`$ActiveRobot`().withPosition(oldPosition).withFuel(100))

    every { gameHandler.getFuelCostForMove(oldPosition, Position(1, 0)) } returns 101
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
    val robot = a(`$ActiveRobot`().withPosition(oldPosition).withFuel(100))
    every { gameHandler.getFuelCostForMove(oldPosition, expectedPosition) } returns 10
    val action = MoveAction(direction)

    // THEN
    val newPosition = action.action(robot, gameHandler)

    // VERIFY
    assertThat(newPosition).isEqualTo(expectedPosition)
    assertThat(robot.fuel).isEqualTo(90)
    assertThat(robot.position).isEqualTo(expectedPosition)
    assertThat(robot.knownPositions).containsExactly(newPosition)

    verify { gameHandler.sendRobotUpdate(robot) }
  }

  @Test
  fun checkJsonForMove() {
    // WHEN
    val move = MoveAction(Direction.EAST)

    // THEN
    val json = objectMapper.writeValueAsString(move)

    // VERIFY
    assertThat(json).isEqualTo("""{"type":"move","direction":"EAST"}""")
  }
}
