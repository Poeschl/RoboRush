package xyz.poeschl.pathseeker.gamelogic

import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import xyz.poeschl.pathseeker.gamelogic.actions.MoveAction
import xyz.poeschl.pathseeker.models.Direction
import xyz.poeschl.pathseeker.repositories.Robot
import xyz.poeschl.pathseeker.security.repository.User
import xyz.poeschl.pathseeker.security.repository.UserRepository
import xyz.poeschl.pathseeker.service.RobotService
import java.util.concurrent.TimeUnit

// @GameLogic
@Profile("!prod")
class DummyBots(
  robotService: RobotService,
  userRepository: UserRepository,
  private val gameHandler: GameHandler,
  private val gameStateService: GameStateMachine
) {

  private val robot1: Robot
  private val robot2: Robot

  init {
    val dummyUser1 = userRepository.findByUsername("dummy1") ?: userRepository.save(User(null, "dummy1", ""))
    val dummyUser2 = userRepository.findByUsername("dummy2") ?: userRepository.save(User(null, "dummy2", ""))
    robot1 = robotService.getRobotByUser(dummyUser1) ?: robotService.createRobot(dummyUser1)
    robot2 = robotService.getRobotByUser(dummyUser2) ?: robotService.createRobot(dummyUser2)
  }

  @Scheduled(fixedRate = 1000, timeUnit = TimeUnit.MILLISECONDS)
  fun dummyRobots() {
    if (gameStateService.isInState(GameState.PREPARE)) {
      gameHandler.registerRobotForNextGame(robot1.id!!)
      gameHandler.registerRobotForNextGame(robot2.id!!)
    } else if (gameStateService.isInState(GameState.WAIT_FOR_ACTION)) {
      val activeRobot1 = gameHandler.getActiveRobot(robot1.id!!)!!
      val activeRobot2 = gameHandler.getActiveRobot(robot2.id!!)!!
      if (activeRobot1.nextAction == null) {
        gameHandler.nextActionForRobot(activeRobot1.id, MoveAction(Direction.entries.random()))
      }
      if (activeRobot2.nextAction == null) {
        gameHandler.nextActionForRobot(activeRobot2.id, MoveAction(Direction.entries.random()))
      }
    }
  }
}