package xyz.poeschl.pathseeker.gamelogic

import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import xyz.poeschl.pathseeker.configuration.GameLogic
import xyz.poeschl.pathseeker.exceptions.InsufficientFuelException
import xyz.poeschl.pathseeker.exceptions.PositionNotAllowedException
import xyz.poeschl.pathseeker.exceptions.PositionOutOfMapException
import xyz.poeschl.pathseeker.gamelogic.actions.MoveAction
import xyz.poeschl.pathseeker.models.ActiveRobot
import xyz.poeschl.pathseeker.models.Color
import xyz.poeschl.pathseeker.models.Direction
import xyz.poeschl.pathseeker.repositories.Robot
import xyz.poeschl.pathseeker.repositories.RobotRepository
import xyz.poeschl.pathseeker.security.repository.User
import xyz.poeschl.pathseeker.security.repository.UserRepository
import java.util.concurrent.TimeUnit

@GameLogic
@Profile("!prod") // The dummy bots are only available when the profile is not "prod" (like inside the docker image)
class DummyBots(
  robotRepository: RobotRepository,
  userRepository: UserRepository,
  private val gameHandler: GameHandler,
  private val gameStateService: GameStateMachine
) {

  companion object {
    // Pass: 12345678
    private const val DUMMY_PASSWORD = "\$2a\$10\$G/P5WCSp0c4YSq9wFrdxBOUEZU4aPSmfvs/Ev4jxIT/r02v2/FRii"
  }

  private val dummyUser1: User = userRepository.findByUsername("dummy1") ?: userRepository.save(User(null, "dummy1", DUMMY_PASSWORD))
  private val dummyUser2: User = userRepository.findByUsername("dummy2") ?: userRepository.save(User(null, "dummy2", DUMMY_PASSWORD))
  private val robot1 = robotRepository.findRobotByUser(dummyUser1) ?: robotRepository.save(Robot(null, Color.randomColor(), dummyUser1))
  private val robot2 = robotRepository.findRobotByUser(dummyUser2) ?: robotRepository.save(Robot(null, Color.randomColor(), dummyUser2))

  @Scheduled(fixedRate = 1000, timeUnit = TimeUnit.MILLISECONDS)
  fun dummyRobots() {
    if (gameStateService.isInState(GameState.PREPARE)) {
      gameHandler.registerRobotForNextGame(robot1.id!!)
      gameHandler.registerRobotForNextGame(robot2.id!!)
    } else if (gameStateService.isInState(GameState.WAIT_FOR_ACTION)) {
      val activeRobot1 = gameHandler.getActiveRobot(robot1.id!!)!!
      val activeRobot2 = gameHandler.getActiveRobot(robot2.id!!)!!

      planNextPossibleRandomMove(activeRobot1)
      planNextPossibleRandomMove(activeRobot2)
    }
  }

  private fun planNextPossibleRandomMove(robot: ActiveRobot) {
    if (robot.nextAction == null && robot.fuel > 0) {
      var moveValid = false
      while (!moveValid) {
        try {
          gameHandler.nextActionForRobot(robot.id, MoveAction(Direction.entries.random()))
          moveValid = true
        } catch (_: PositionNotAllowedException) {
        } catch (_: PositionOutOfMapException) {
        } catch (_: InsufficientFuelException) {
        }
      }
    }
  }
}
