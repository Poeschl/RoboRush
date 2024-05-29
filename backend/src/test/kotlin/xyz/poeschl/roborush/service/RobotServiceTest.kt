package xyz.poeschl.roborush.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import xyz.poeschl.roborush.exceptions.RobotNotActiveException
import xyz.poeschl.roborush.gamelogic.GameHandler
import xyz.poeschl.roborush.gamelogic.actions.MoveAction
import xyz.poeschl.roborush.gamelogic.actions.RefuelAction
import xyz.poeschl.roborush.gamelogic.actions.ScanAction
import xyz.poeschl.roborush.gamelogic.actions.WaitAction
import xyz.poeschl.roborush.models.PublicRobot
import xyz.poeschl.roborush.models.ScoreboardEntry
import xyz.poeschl.roborush.repositories.RobotRepository
import xyz.poeschl.roborush.test.utils.builder.Builders.Companion.a
import xyz.poeschl.roborush.test.utils.builder.GameLogicBuilder.Companion.`$ActiveRobot`
import xyz.poeschl.roborush.test.utils.builder.GameLogicBuilder.Companion.`$Direction`
import xyz.poeschl.roborush.test.utils.builder.GameLogicBuilder.Companion.`$Robot`
import xyz.poeschl.roborush.test.utils.builder.SecurityBuilder.Companion.`$User`

class RobotServiceTest {

  private val robotRepository = mockk<RobotRepository>(relaxUnitFun = true)
  private val gameHandler = mockk<GameHandler>(relaxUnitFun = true)

  private val robotService = RobotService(robotRepository, gameHandler)

  @Test
  fun getRobotByUser() {
    // WHEN
    val user = a(`$User`())
    val robot = a(`$Robot`().withId(1L).withUser(user))
    every { robotRepository.findRobotByUser(user) } returns robot

    // THEN
    val result = robotService.getRobotByUser(user)

    // VERIFY
    assertThat(result).isEqualTo(robot)
  }

  @Test
  fun getActiveRobotByUser() {
    // WHEN
    val user = a(`$User`())
    val robot = a(`$Robot`().withId(1L).withUser(user))
    val activeRobot = a(`$ActiveRobot`().withId(1))
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
    val user = a(`$User`())
    every { robotRepository.findRobotByUser(user) } returns null

    // THEN
    val result = robotService.getActiveRobotByUser(user)

    // VERIFY
    assertThat(result).isNull()
  }

  @Test
  fun getActiveRobotByUser_robotNotActive() {
    // WHEN
    val user = a(`$User`())
    val robot = a(`$Robot`().withId(1L).withUser(user))
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
    val user = a(`$User`())
    val activeRobot = a(`$ActiveRobot`().withId(1))

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
    val user = a(`$User`())

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
    val activeRobot1 = a(`$ActiveRobot`().withId(1))
    val activeRobot2 = a(`$ActiveRobot`().withId(2))

    every { gameHandler.getActiveRobots() } returns setOf(activeRobot2, activeRobot1)

    // THEN
    val result = robotService.getActiveRobots()

    // VERIFY
    assertThat(result).containsExactly(
      PublicRobot(activeRobot1.id, activeRobot1.name, activeRobot1.color, activeRobot1.position),
      PublicRobot(activeRobot2.id, activeRobot2.name, activeRobot2.color, activeRobot2.position)
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
    val direction = a(`$Direction`())

    // THEN
    robotService.scheduleMove(robotId, direction)

    // VERIFY
    verify {
      gameHandler.nextActionForRobot(robotId, MoveAction(direction))
    }
  }

  @Test
  fun scheduleWait() {
    // WHEN
    val robotId = 1L

    // THEN
    robotService.scheduleWait(robotId)

    // VERIFY
    verify {
      gameHandler.nextActionForRobot(robotId, WaitAction())
    }
  }

  @Test
  fun scheduleRefuel() {
    // WHEN
    val robotId = 1L

    // THEN
    robotService.scheduleRefuel(robotId)

    // VERIFY
    verify {
      gameHandler.nextActionForRobot(robotId, RefuelAction())
    }
  }

  @Test
  fun getTopRobots() {
    // WHEN
    val robot1 = a(`$Robot`().withUser(a(`$User`().withUsername("Hans"))).withScore(100))
    val robot2 = a(`$Robot`().withUser(a(`$User`().withUsername("Hänschen"))).withScore(50))

    every { robotRepository.findTop10Robots() } returns listOf(robot1, robot2)

    // THEN
    val result = robotService.getTopRobots()

    // VERIFY
    assertThat(result[0]).isEqualTo(ScoreboardEntry(robot1.user.username, robot1.color, robot1.score))
    assertThat(result[1]).isEqualTo(ScoreboardEntry(robot2.user.username, robot2.color, robot2.score))
  }
}
