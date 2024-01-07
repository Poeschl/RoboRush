package xyz.poeschl.pathseeker.service

import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import xyz.poeschl.pathseeker.exceptions.RobotNotActiveException
import xyz.poeschl.pathseeker.gamelogic.GameHandler
import xyz.poeschl.pathseeker.gamelogic.actions.MoveAction
import xyz.poeschl.pathseeker.gamelogic.actions.ScanAction
import xyz.poeschl.pathseeker.models.*
import xyz.poeschl.pathseeker.repositories.Robot
import xyz.poeschl.pathseeker.repositories.RobotRepository
import xyz.poeschl.pathseeker.security.repository.User

class RobotServiceTest {

  private val robotRepository = mockk<RobotRepository>(relaxUnitFun = true)
  private val gameHandler = mockk<GameHandler>(relaxUnitFun = true)

  private val robotService = RobotService(robotRepository, gameHandler)

  @Test
  fun createRobot() {
    // WHEN
    val user = User("test", "")

    every { robotRepository.save(any()) } answers { firstArg() }

    // THEN
    val result = robotService.createRobot(user)

    // VERIFY
    val robotSlot = slot<Robot>()
    verify {
      robotRepository.save(capture(robotSlot))
    }
    assertThat(result).isEqualTo(robotSlot.captured)
    assertThat(result.user).isEqualTo(user)
  }

  @Test
  fun getRobotByUser() {
    // WHEN
    val user = User(1L, "dummy", "")
    val robot = Robot(1L, Color(1, 2, 3), user)
    every { robotRepository.findRobotByUser(user) } returns robot

    // THEN
    val result = robotService.getRobotByUser(user)

    // VERIFY
    assertThat(result).isEqualTo(robot)
  }

  @Test
  fun getActiveRobotByUser() {
    // WHEN
    val user = User("dummy", "")
    val robot = Robot(1L, Color(1, 2, 3), user)
    val activeRobot = ActiveRobot(1L, Color(1, 2, 3), 100, Position(0, 0))
    every { robotRepository.findRobotByUser(user) } returns robot
    every { gameHandler.getActiveRobot(robot.id!!) } returns activeRobot

    // THEN
    val result = robotService.getActiveRobotByUser(user)

    // VERIFY
    assertThat(result).isEqualTo(activeRobot)
  }

  @Test
  fun getActiveRobotByUser_robotUnknown() {
    // WHEN
    val user = User("dummy", "")
    every { robotRepository.findRobotByUser(user) } returns null

    // THEN
    val result = robotService.getActiveRobotByUser(user)

    // VERIFY
    assertThat(result).isNull()
  }

  @Test
  fun getActiveRobotByUser_robotNotActive() {
    // WHEN
    val user = User("dummy", "")
    val robot = Robot(1L, Color(1, 2, 3), user)
    every { robotRepository.findRobotByUser(user) } returns robot
    every { gameHandler.getActiveRobot(robot.id!!) } returns null

    // THEN
    val result = robotService.getActiveRobotByUser(user)

    // VERIFY
    assertThat(result).isNull()
  }

  @Test
  fun executeWithActiveRobotIdOfUser() {
    // WHEN
    val user = User("dummy", "1234")
    val activeRobot = ActiveRobot(1L, Color(1, 2, 3), 100, Position(0, 0))
    var actionExecuted = false

    val robotServiceSpy = spyk(robotService)
    every { robotServiceSpy.getActiveRobotByUser(user) } returns activeRobot

    // THEN
    robotServiceSpy.executeWithActiveRobotIdOfUser(user) { _: Long -> actionExecuted = true }

    // VERIFY
    assertThat(actionExecuted).isTrue()
  }

  @Test
  fun executeWithActiveRobotIdOfUser_noActiveRobot() {
    // WHEN
    val user = User("dummy", "1234")

    val robotServiceSpy = spyk(robotService)
    every { robotServiceSpy.getActiveRobotByUser(user) } returns null

    // THEN
    assertThrows<RobotNotActiveException> {
      robotServiceSpy.executeWithActiveRobotIdOfUser(user) { }
    }

    // VERIFY
  }

  @Test
  fun getActiveRobots() {
    // WHEN
    val activeRobot1 = ActiveRobot(1L, Color(1, 2, 3), 100, Position(0, 0))
    val activeRobot2 = ActiveRobot(2L, Color(1, 2, 3), 100, Position(0, 0))

    every { gameHandler.getActiveRobots() } returns setOf(activeRobot2, activeRobot1)

    // THEN
    val result = robotService.getActiveRobots()

    // VERIFY
    assertThat(result).containsExactly(
      PublicRobot(activeRobot1.id, activeRobot1.color, activeRobot1.position),
      PublicRobot(activeRobot2.id, activeRobot2.color, activeRobot2.position)
    )
  }

  @Test
  fun registerRobotForGame() {
    // WHEN
    val robotId = 1L

    // THEN
    robotService.registerRobotForGame(robotId)

    // VERIFY
    verify {
      gameHandler.registerRobotForNextGame(robotId)
    }
  }

  @Test
  fun scheduleScan() {
    // WHEN
    val robotId = 1L
    val distance = 10

    // THEN
    robotService.scheduleScan(robotId, distance)

    // VERIFY
    verify {
      gameHandler.nextActionForRobot(robotId, ScanAction(distance))
    }
  }

  @Test
  fun scheduleMove() {
    // WHEN
    val robotId = 1L
    val direction = Direction.NORTH

    // THEN
    robotService.scheduleMove(robotId, direction)

    // VERIFY
    verify {
      gameHandler.nextActionForRobot(robotId, MoveAction(direction))
    }
  }
}
