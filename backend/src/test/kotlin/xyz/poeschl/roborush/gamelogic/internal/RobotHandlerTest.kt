package xyz.poeschl.roborush.gamelogic.internal

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import xyz.poeschl.roborush.exceptions.GameStateException
import xyz.poeschl.roborush.gamelogic.GameHandler
import xyz.poeschl.roborush.gamelogic.GameState
import xyz.poeschl.roborush.gamelogic.GameStateMachine
import xyz.poeschl.roborush.gamelogic.actions.MoveAction
import xyz.poeschl.roborush.gamelogic.actions.WaitAction
import xyz.poeschl.roborush.models.ActiveRobot
import xyz.poeschl.roborush.models.Direction
import xyz.poeschl.roborush.models.Position
import xyz.poeschl.roborush.repositories.RobotRepository
import xyz.poeschl.roborush.service.PlayedGamesService
import xyz.poeschl.roborush.test.utils.builder.Builders.Companion.a
import xyz.poeschl.roborush.test.utils.builder.GameLogicBuilder.Companion.`$Direction`
import xyz.poeschl.roborush.test.utils.builder.GameLogicBuilder.Companion.`$Robot`
import xyz.poeschl.roborush.test.utils.builder.SecurityBuilder.Companion.`$User`
import java.util.*

class RobotHandlerTest {

  private val robotRepository = mockk<RobotRepository>()
  private val playedGamesService = mockk<PlayedGamesService>()
  private val gameStateMachine = mockk<GameStateMachine>()
  private val gameHandler = mockk<GameHandler>()
  private val robotHandler = RobotHandler(playedGamesService, robotRepository, gameStateMachine)

  @Test
  fun registerRobot() {
    // WHEN
    val robotId = 1L
    val user = a(`$User`())
    val robot = a(`$Robot`().withId(1).withUser(user))
    val position = Position(1, 2)

    every { gameStateMachine.isInState(GameState.WAIT_FOR_PLAYERS) } returns true
    every { robotRepository.findById(robotId) } returns Optional.of(robot)

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

    every { gameStateMachine.isInState(GameState.WAIT_FOR_PLAYERS) } returns false

    // THEN
    assertThrows<GameStateException> {
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

    every { gameStateMachine.isInState(GameState.WAIT_FOR_PLAYERS) } returns true
    every { robotRepository.findById(robotId) } returns Optional.empty()

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
    val user = a(`$User`())
    val robot = a(`$Robot`().withId(1).withUser(user))
    val position1 = Position(1, 2)
    val position2 = Position(1, 2)

    every { gameStateMachine.isInState(GameState.WAIT_FOR_PLAYERS) } returns true
    every { robotRepository.findById(robotId) } returns Optional.of(robot)

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
    val user1 = a(`$User`().withId(1))
    val user2 = a(`$User`().withId(1))
    val robotId1 = 1L
    val robotId2 = 1L
    val robot1 = a(`$Robot`().withId(1).withUser(user1))
    val robot2 = a(`$Robot`().withId(1).withUser(user2))
    val position = Position(1, 2)

    every { gameStateMachine.isInState(GameState.WAIT_FOR_PLAYERS) } returns true
    every { robotRepository.findById(robotId1) } returns Optional.of(robot1)
    every { robotRepository.findById(robotId2) } returns Optional.of(robot2)

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
  fun setRobotMaxFuel() {
    // WHEN
    val maxFuel = 1234

    // THEN
    robotHandler.setRobotMaxFuel(maxFuel)

    // VERIFY
    val newRobot = createSingleActiveRobot()
    assertThat(newRobot.fuel).isEqualTo(maxFuel)
    assertThat(newRobot.maxFuel).isEqualTo(maxFuel)
  }

  @Test
  fun setNextMove() {
    // WHEN
    val robot = createSingleActiveRobot()
    every { gameStateMachine.isInState(GameState.WAIT_FOR_ACTION) } returns true
    val action = mockk<MoveAction>(relaxUnitFun = true)

    // THEN
    robotHandler.setNextMove(robot.id, gameHandler, action)

    // VERIFY
    assertThat(robot.nextAction).isEqualTo(action)
    verify {
      action.check(robot, gameHandler)
    }
  }

  @Test
  fun setNextMove_invalidState() {
    // WHEN
    val robot = createSingleActiveRobot()
    every { gameStateMachine.isInState(GameState.WAIT_FOR_ACTION) } returns false
    val action = mockk<MoveAction>()

    // THEN
    assertThrows<GameStateException> {
      robotHandler.setNextMove(robot.id, gameHandler, action)
    }

    // VERIFY
    assertThat(robot.nextAction).isNull()
    verify(exactly = 0) { action.check(robot, gameHandler) }
  }

  @Test
  fun setNextMove_unknownRobot() {
    // WHEN
    every { gameStateMachine.isInState(GameState.WAIT_FOR_ACTION) } returns true
    val action = mockk<MoveAction>()

    // THEN
    robotHandler.setNextMove(123, gameHandler, action)

    // VERIFY
    // nothing happens
  }

  @Test
  fun executeRobotActions() {
    // WHEN
    val robot = createSingleActiveRobot()
    val action = mockk<MoveAction>()
    val returnValue = Position(1, 2)
    robot.nextAction = action

    every { gameStateMachine.isInState(GameState.ACTION) } returns true
    every { action.action(robot, gameHandler) } returns returnValue

    // THEN
    robotHandler.executeRobotActions(gameHandler)

    // VERIFY
    verify { action.action(robot, gameHandler) }
    assertThat(robot.nextAction).isNull()
    assertThat(robot.lastResult).isEqualTo(returnValue)
  }

  @Test
  fun executeRobotActions_invalidState() {
    // WHEN
    val robot = createSingleActiveRobot()
    val action = mockk<MoveAction>()
    robot.nextAction = action

    every { gameStateMachine.isInState(GameState.ACTION) } returns false

    // THEN
    assertThrows<GameStateException> {
      robotHandler.executeRobotActions(gameHandler)
    }

    // VERIFY
    verify(exactly = 0) { action.action(robot, gameHandler) }
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
  fun getACurrentlyFreePosition() {
    // WHEN
    val existingRobot = createSingleActiveRobot()
    val freePosition = Position(1, 2)
    val positionsToValidate = listOf(existingRobot.position, freePosition)

    // THEN
    val result = robotHandler.getACurrentlyFreePosition(positionsToValidate)

    // VERIFY
    assertThat(result).isEqualTo(freePosition)
  }

  @Test
  fun getACurrentlyFreePosition_noneFound() {
    // WHEN
    val existingRobot = createSingleActiveRobot()
    val positionsToValidate = listOf(existingRobot.position)

    // THEN
    val result = robotHandler.getACurrentlyFreePosition(positionsToValidate)

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
    createSingleActiveRobot(1)

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
    createSingleActiveRobot(1)

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

  @Test
  fun isEveryRobotIdle() {
    // WHEN
    val robot1 = createSingleActiveRobot(1)
    val robot2 = createSingleActiveRobot(2)
    createSingleActiveRobot(3)
    robot1.nextAction = MoveAction(a(`$Direction`()))
    robot2.nextAction = MoveAction(a(`$Direction`()))

    // THEN
    val idle = robotHandler.isEveryRobotIdle()

    // VERIFY
    assertThat(idle).isFalse()
  }

  @Test
  fun isEveryRobotIdle_idle() {
    // WHEN
    val robot1 = createSingleActiveRobot(1)
    val robot2 = createSingleActiveRobot(2)
    robot1.nextAction = null
    robot2.nextAction = null

    // THEN
    val idle = robotHandler.isEveryRobotIdle()

    // VERIFY
    assertThat(idle).isTrue()
  }

  @Test
  fun isEveryRobotIdle_waitAction() {
    // WHEN
    val robot1 = createSingleActiveRobot(1)
    val robot2 = createSingleActiveRobot(2)
    robot1.nextAction = WaitAction()
    robot2.nextAction = null

    // THEN
    val idle = robotHandler.isEveryRobotIdle()

    // VERIFY
    assertThat(idle).isTrue()
  }

  /***
   * Creates a single registered robot with given id
   * Uses the public methods for it.
   *
   * @param robotId The id for the new robot, or 1 as default
   * @return The generated and registered robot.
   */
  private fun createSingleActiveRobot(robotId: Long = 1): ActiveRobot {
    val user = a(`$User`())
    val robot = a(`$Robot`().withId(robotId).withUser(user))
    val position = Position((2 + robotId).toInt(), (2 + robotId).toInt())

    every { gameStateMachine.isInState(GameState.WAIT_FOR_PLAYERS) } returns true
    every { robotRepository.findById(robotId) } returns Optional.of(robot)

    robotHandler.registerRobotForGame(robotId, position)
    val savedRobot = robotHandler.getActiveRobot(robotId)!!
    assertThat(savedRobot).isNotNull()
    return savedRobot
  }
}
