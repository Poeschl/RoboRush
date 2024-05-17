package xyz.poeschl.roborush.gamelogic

import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import xyz.poeschl.roborush.configuration.GameLogic
import xyz.poeschl.roborush.exceptions.InsufficientFuelException
import xyz.poeschl.roborush.exceptions.PositionNotAllowedException
import xyz.poeschl.roborush.exceptions.PositionOutOfMapException
import xyz.poeschl.roborush.gamelogic.actions.MoveAction
import xyz.poeschl.roborush.models.ActiveRobot
import xyz.poeschl.roborush.models.Color
import xyz.poeschl.roborush.models.Direction
import xyz.poeschl.roborush.repositories.Robot
import xyz.poeschl.roborush.repositories.RobotRepository
import xyz.poeschl.roborush.security.repository.User
import xyz.poeschl.roborush.security.repository.UserRepository
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
  private val dummyUser3: User = userRepository.findByUsername("dummy3") ?: userRepository.save(User(null, "dummy3", DUMMY_PASSWORD))
  private val robot1 = robotRepository.findRobotByUser(dummyUser1) ?: robotRepository.save(Robot(null, Color.randomColor(), dummyUser1))
  private val robot2 = robotRepository.findRobotByUser(dummyUser2) ?: robotRepository.save(Robot(null, Color.randomColor(), dummyUser2))
  private val robot3 = robotRepository.findRobotByUser(dummyUser3) ?: robotRepository.save(Robot(null, Color.randomColor(), dummyUser3))

  @Scheduled(fixedRate = 1000, timeUnit = TimeUnit.MILLISECONDS)
  fun dummyRobots() {
    if (gameStateService.isInState(GameState.WAIT_FOR_PLAYERS)) {
      gameHandler.registerRobotForNextGame(robot1.id!!)
      gameHandler.registerRobotForNextGame(robot2.id!!)
      gameHandler.registerRobotForNextGame(robot3.id!!)
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
      var triesLeft = 10
      while (!moveValid && triesLeft > 0) {
        try {
          gameHandler.nextActionForRobot(robot.id, MoveAction(Direction.entries.random()))
          moveValid = true
        } catch (_: PositionNotAllowedException) {
        } catch (_: PositionOutOfMapException) {
        } catch (_: InsufficientFuelException) {
        }
        triesLeft--
      }
    }
  }
}
