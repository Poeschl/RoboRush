package xyz.poeschl.roborush.gamelogic.dummybots

import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import xyz.poeschl.roborush.configuration.GameLogic
import xyz.poeschl.roborush.gamelogic.GameHandler
import xyz.poeschl.roborush.gamelogic.GameStateMachine
import xyz.poeschl.roborush.models.Color
import xyz.poeschl.roborush.repositories.Robot
import xyz.poeschl.roborush.repositories.RobotRepository
import xyz.poeschl.roborush.security.repository.User
import xyz.poeschl.roborush.security.repository.UserRepository
import java.util.concurrent.TimeUnit

@GameLogic
@Profile(
  "dummybots | !prod"
) // The dummy bots are only available when the profile "dummybots" is set, or it's not a prod environment (like inside the docker image)
class DummyBots(
  gameHandler: GameHandler,
  private val robotRepository: RobotRepository,
  private val userRepository: UserRepository,
  private val gameStateService: GameStateMachine
) {

  companion object {
    // Pass: 12345678
    private const val DUMMY_PASSWORD = "\$2a\$10\$G/P5WCSp0c4YSq9wFrdxBOUEZU4aPSmfvs/Ev4jxIT/r02v2/FRii"
  }

  private val robot1 = createOrGetRobot("dummy1", 1)
  private val robot2 = createOrGetRobot("dummy2")
  private val robot3 = createOrGetRobot("dummy3")

  private val bots =
    listOf(WallHuggerBot(gameHandler, robot1), Bot(gameHandler, robot2), ChillerBot(gameHandler, robot3))

  @Scheduled(fixedRate = 1000, timeUnit = TimeUnit.MILLISECONDS)
  fun dummyRobots() {
    bots.forEach { it.doSomething(gameStateService.getCurrentState()) }
  }

  private fun createOrGetRobot(username: String, score: Long = 0): Robot {
    val user: User = userRepository.findByUsername(username) ?: userRepository.save(User(null, username, DUMMY_PASSWORD))
    return robotRepository.findRobotByUser(user) ?: robotRepository.save(Robot(null, Color.randomColor(), score, user))
  }
}
