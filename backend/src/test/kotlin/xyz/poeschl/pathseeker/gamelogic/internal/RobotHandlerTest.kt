package xyz.poeschl.pathseeker.gamelogic.internal

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import xyz.poeschl.pathseeker.exceptions.InvalidGameStateException
import xyz.poeschl.pathseeker.gamelogic.GameHandler
import xyz.poeschl.pathseeker.gamelogic.GameState
import xyz.poeschl.pathseeker.gamelogic.GameStateMachine
import xyz.poeschl.pathseeker.gamelogic.actions.MoveAction
import xyz.poeschl.pathseeker.models.ActiveRobot
import xyz.poeschl.pathseeker.models.Color
import xyz.poeschl.pathseeker.models.Direction
import xyz.poeschl.pathseeker.models.Position
import xyz.poeschl.pathseeker.repositories.Robot
import xyz.poeschl.pathseeker.repositories.RobotRepository
import xyz.poeschl.pathseeker.security.repository.User
import java.util.*

class RobotHandlerTest {

  private val robotRepository = mock<RobotRepository>()
  private val gameStateMachine = mock<GameStateMachine>()
  private val gameHandler = mock<GameHandler>()
  private val robotHandler = RobotHandler(robotRepository, gameStateMachine)

  @Test
  fun registerRobot() {
    // WHEN
    val robotId = 1L
    val user = User("test", "")
    val robot = Robot(1, Color(1, 2, 3), user)
    val position = Position(1, 2)

    `when`(gameStateMachine.isInState(GameState.PREPARE)).thenReturn(true)
    `when`(robotRepository.findById(robotId)).thenReturn(Optional.of(robot))

    // THEN
    robotHandler.registerRobotForGame(robotId, position)

    // VERIFY
    val registeredRobots = robotHandler.getAllActiveRobots()
    assertThat(registeredRobots).hasSize(1)
    val registered = registeredRobots.first()
    assertThat(registered.id).isEqualTo(robotId)
    assertThat(registered.position).isEqualTo(position)
  }

  @Test
  fun registerRobot_invalidState() {
    // WHEN
    val robotId = 1L
    val position = Position(1, 2)

    `when`(gameStateMachine.isInState(GameState.PREPARE)).thenReturn(false)

    // THEN
    assertThrows<InvalidGameStateException> {
      robotHandler.registerRobotForGame(robotId, position)
    }

    // VERIFY
    val registeredRobots = robotHandler.getAllActiveRobots()
    assertThat(registeredRobots).isEmpty()
  }

  @Test
  fun registerRobot_unknownRobotId() {
    // WHEN
    val robotId = 1L
    val position = Position(1, 2)

    `when`(gameStateMachine.isInState(GameState.PREPARE)).thenReturn(true)
    `when`(robotRepository.findById(robotId)).thenReturn(Optional.empty())

    // THEN
    robotHandler.registerRobotForGame(robotId, position)

    // VERIFY
    val registeredRobots = robotHandler.getAllActiveRobots()
    assertThat(registeredRobots).isEmpty()
  }

  @Test
  fun registerRobot_duplicateRegistration() {
    // WHEN
    val robotId = 1L
    val user = User("test", "")
    val robot = Robot(1, Color(1, 2, 3), user)
    val position1 = Position(1, 2)
    val position2 = Position(1, 2)

    `when`(gameStateMachine.isInState(GameState.PREPARE)).thenReturn(true)
    `when`(robotRepository.findById(robotId)).thenReturn(Optional.of(robot))

    // THEN
    robotHandler.registerRobotForGame(robotId, position1)
    robotHandler.registerRobotForGame(robotId, position2)

    // VERIFY
    val registeredRobots = robotHandler.getAllActiveRobots()
    assertThat(registeredRobots).hasSize(1)
    val registered = registeredRobots.first()
    assertThat(registered.id).isEqualTo(robotId)
    assertThat(registered.position).isEqualTo(position1)
  }

  @Test
  fun registerRobot_occupiedStartPosition() {
    // WHEN
    val user1 = User("test1", "")
    val user2 = User("test2", "")
    val robotId1 = 1L
    val robotId2 = 1L
    val robot1 = Robot(1, Color(1, 2, 3), user1)
    val robot2 = Robot(1, Color(1, 2, 3), user2)
    val position = Position(1, 2)

    `when`(gameStateMachine.isInState(GameState.PREPARE)).thenReturn(true)
    `when`(robotRepository.findById(robotId1)).thenReturn(Optional.of(robot1))
    `when`(robotRepository.findById(robotId2)).thenReturn(Optional.of(robot2))

    // THEN
    robotHandler.registerRobotForGame(robotId1, position)
    robotHandler.registerRobotForGame(robotId2, position)

    // VERIFY
    val registeredRobots = robotHandler.getAllActiveRobots()
    assertThat(registeredRobots).hasSize(1)
    val registered = registeredRobots.first()
    assertThat(registered.id).isEqualTo(robotId1)
    assertThat(registered.position).isEqualTo(position)
  }

  @Test
  fun clear() {
    // WHEN
    createSingleActiveRobot()

    // THEN
    robotHandler.clearActiveRobots()

    // VERIFY
    assertThat(robotHandler.getAllActiveRobots()).isEmpty()
  }

  @Test
  fun setNextMove() {
    // WHEN
    val robot = createSingleActiveRobot()
    `when`(gameStateMachine.isInState(GameState.WAIT_FOR_ACTION)).thenReturn(true)
    val action = mock<MoveAction>()

    // THEN
    robotHandler.setNextMove(robot.id, gameHandler, action)

    // VERIFY
    assertThat(robot.nextAction).isEqualTo(action)
    verify(action).check(robot, gameHandler)
  }

  @Test
  fun setNextMove_invalidState() {
    // WHEN
    val robot = createSingleActiveRobot()
    `when`(gameStateMachine.isInState(GameState.WAIT_FOR_ACTION)).thenReturn(false)
    val action = mock<MoveAction>()

    // THEN
    assertThrows<InvalidGameStateException> {
      robotHandler.setNextMove(robot.id, gameHandler, action)
    }

    // VERIFY
    assertThat(robot.nextAction).isNull()
    verify(action, never()).check(robot, gameHandler)
  }

  @Test
  fun setNextMove_unknownRobot() {
    // WHEN
    `when`(gameStateMachine.isInState(GameState.WAIT_FOR_ACTION)).thenReturn(true)
    val action = mock<MoveAction>()

    // THEN
    robotHandler.setNextMove(123, gameHandler, action)

    // VERIFY
    // nothing happens
  }

  @Test
  fun executeRobotActions() {
    // WHEN
    val robot = createSingleActiveRobot()
    val action = mock<MoveAction>()
    robot.nextAction = action

    `when`(gameStateMachine.isInState(GameState.ACTION)).thenReturn(true)
    `when`(action.action(robot, gameHandler)).thenReturn(null)

    // THEN
    robotHandler.executeRobotActions(gameHandler)

    // VERIFY
    verify(action).action(robot, gameHandler)
    assertThat(robot.nextAction).isNull()
    assertThat(robot.lastResult).isNull()
  }

  @Test
  fun executeRobotActions_withReturn() {
    // WHEN
    val robot = createSingleActiveRobot()
    val action = mock<MoveAction>()
    val returnValue = Position(1, 2)
    robot.nextAction = action

    `when`(gameStateMachine.isInState(GameState.ACTION)).thenReturn(true)
    `when`(action.action(robot, gameHandler)).thenReturn(returnValue)

    // THEN
    robotHandler.executeRobotActions(gameHandler)

    // VERIFY
    verify(action).action(robot, gameHandler)
    assertThat(robot.nextAction).isNull()
    assertThat(robot.lastResult).isEqualTo(returnValue)
  }

  @Test
  fun executeRobotActions_invalidState() {
    // WHEN
    val robot = createSingleActiveRobot()
    val action = mock<MoveAction>()
    robot.nextAction = action

    `when`(gameStateMachine.isInState(GameState.ACTION)).thenReturn(false)

    // THEN
    assertThrows<InvalidGameStateException> {
      robotHandler.executeRobotActions(gameHandler)
    }

    // VERIFY
    verify(action, never()).action(robot, gameHandler)
    assertThat(robot.nextAction).isEqualTo(action)
  }

  @Test
  fun getActiveRobot() {
    // WHEN
    createSingleActiveRobot(1)
    createSingleActiveRobot(3)
    val robotToSearch = createSingleActiveRobot(2)

    // THEN
    val foundRobot = robotHandler.getActiveRobot(robotToSearch.id)

    // VERIFY
    assertThat(foundRobot).isEqualTo(robotToSearch)
  }

  @Test
  fun getActiveRobot_notFound() {
    // WHEN
    createSingleActiveRobot(1)
    createSingleActiveRobot(3)
    createSingleActiveRobot(2)

    // THEN
    val foundRobot = robotHandler.getActiveRobot(666)

    // VERIFY
    assertThat(foundRobot).isNull()
  }

  @Test
  fun getAllActiveRobots() {
    // WHEN
    val robot1 = createSingleActiveRobot(1)
    val robot2 = createSingleActiveRobot(3)
    val robot3 = createSingleActiveRobot(2)

    // THEN
    val foundRobots = robotHandler.getAllActiveRobots()

    // VERIFY
    assertThat(foundRobots).containsExactlyInAnyOrder(robot1, robot2, robot3)
  }

  @Test
  fun getFirstCurrentlyFreePosition() {
    // WHEN
    val existingRobot = createSingleActiveRobot()
    val freePosition = Position(1, 2)
    val positionsToValidate = listOf(existingRobot.position, freePosition)

    // THEN
    val result = robotHandler.getFirstCurrentlyFreePosition(positionsToValidate)

    // VERIFY
    assertThat(result).isEqualTo(freePosition)
  }

  @Test
  fun getFirstCurrentlyFreePosition_noneFound() {
    // WHEN
    val existingRobot = createSingleActiveRobot()
    val positionsToValidate = listOf(existingRobot.position)

    // THEN
    val result = robotHandler.getFirstCurrentlyFreePosition(positionsToValidate)

    // VERIFY
    assertThat(result).isNull()
  }

  @Test
  fun isPositionCurrentFree() {
    // WHEN
    createSingleActiveRobot()
    val freePosition = Position(1, 2)

    // THEN
    val result = robotHandler.isPositionCurrentFree(freePosition)

    // VERIFY
    assertThat(result).isTrue()
  }

  @Test
  fun isPositionCurrentFree_occupied() {
    // WHEN
    val existingRobot = createSingleActiveRobot()

    // THEN
    val result = robotHandler.isPositionCurrentFree(existingRobot.position)

    // VERIFY
    assertThat(result).isFalse()
  }

  @Test
  fun isPositionCurrentFree_noRobot() {
    // WHEN

    // THEN
    val result = robotHandler.isPositionCurrentFree(Position(0, 0))

    // VERIFY
    assertThat(result).isTrue()
  }

  @Test
  fun isPositionFreeAfterActions() {
    // WHEN
    // stayRobot will be at position (3,3)
    val stayRobot = createSingleActiveRobot(1)

    // THEN
    val isFree = robotHandler.isPositionFreeAfterActions(Position(2, 2))

    // VERIFY
    // should be free since no robot stands on the position
    assertThat(isFree).isTrue()
  }

  @Test
  fun isPositionFreeAfterActions_occupied() {
    // WHEN
    // stayRobot will be at position (3,3)
    val stayRobot = createSingleActiveRobot(1)

    // THEN
    val isFree = robotHandler.isPositionFreeAfterActions(Position(3, 3))

    // VERIFY
    // should be occupied since stayrobot stands there
    assertThat(isFree).isFalse()
  }

  @Test
  fun isPositionFreeAfterActions_movingRobot() {
    // WHEN
    // movingRobot will start at position (4,4)
    val movingRobot = createSingleActiveRobot(2)
    // movingRobot moves to (4,3)
    movingRobot.nextAction = MoveAction(Direction.NORTH)

    // THEN
    val isFree = robotHandler.isPositionFreeAfterActions(Position(4, 4))

    // VERIFY
    // should be free since movingRobot will move away in the next turn
    assertThat(isFree).isTrue()
  }

  @Test
  fun isPositionFreeAfterActions_movingRobot_occupied() {
    // WHEN
    // movingRobot will start at position (4,4)
    val movingRobot = createSingleActiveRobot(2)
    // movingRobot moves to (4,3)
    movingRobot.nextAction = MoveAction(Direction.NORTH)

    // THEN
    val isFree = robotHandler.isPositionFreeAfterActions(Position(4, 3))

    // VERIFY
    // should be occupied since movingRobot will move there in the next turn
    assertThat(isFree).isFalse()
  }

  /***
   * Creates a single registered robot with given id
   * Uses the public methods for it.
   *
   * @param robotId The id for the new robot, or 1 as default
   * @return The generated and registered robot.
   */
  private fun createSingleActiveRobot(robotId: Long = 1): ActiveRobot {
    val user = User("test", "")
    val robot = Robot(robotId, Color(1, 2, 3), user)
    val position = Position((2 + robotId).toInt(), (2 + robotId).toInt())

    `when`(gameStateMachine.isInState(GameState.PREPARE)).thenReturn(true)
    `when`(robotRepository.findById(robotId)).thenReturn(Optional.of(robot))

    robotHandler.registerRobotForGame(robotId, position)
    val savedRobot = robotHandler.getActiveRobot(robotId)!!
    assertThat(savedRobot).isNotNull()
    return savedRobot
  }
}
