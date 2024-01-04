package xyz.poeschl.pathseeker.gamelogic

import org.springframework.scheduling.annotation.Scheduled
import xyz.poeschl.pathseeker.configuration.GameLogic
import xyz.poeschl.pathseeker.gamelogic.actions.MoveAction
import xyz.poeschl.pathseeker.gamelogic.internal.RobotHandler
import xyz.poeschl.pathseeker.models.Direction
import xyz.poeschl.pathseeker.repositories.RobotRepository
import java.util.concurrent.TimeUnit

@GameLogic
class DummyBots(
  robotRepository: RobotRepository,
  robotHandler: RobotHandler,
  private val gameHandler: GameHandler,
  private val gameStateService: GameStateMachine
) {

  private val robot1 = robotRepository.findById(1L).orElseGet { robotHandler.createRobot() }
  private val robot2 = robotRepository.findById(2L).orElseGet { robotHandler.createRobot() }

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
