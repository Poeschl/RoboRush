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
  robotRepository: RobotRepository,
  userRepository: UserRepository,
  gameHandler: GameHandler,
  private val gameStateService: GameStateMachine
) {

  companion object {
    // Pass: 12345678
    private const val DUMMY_PASSWORD = "\$2a\$10\$G/P5WCSp0c4YSq9wFrdxBOUEZU4aPSmfvs/Ev4jxIT/r02v2/FRii"
  }

  private val dummyUser1: User = userRepository.findByUsername("dummy1") ?: userRepository.save(User(null, "dummy1", DUMMY_PASSWORD))
  private val dummyUser2: User = userRepository.findByUsername("dummy2") ?: userRepository.save(User(null, "dummy2", DUMMY_PASSWORD))
  private val dummyUser3: User = userRepository.findByUsername("dummy3") ?: userRepository.save(User(null, "dummy3", DUMMY_PASSWORD))
  private val robot1 = robotRepository.findRobotByUser(dummyUser1) ?: robotRepository.save(Robot(null, Color.randomColor(), 1, dummyUser1))
  private val robot2 = robotRepository.findRobotByUser(dummyUser2) ?: robotRepository.save(Robot(null, Color.randomColor(), 0, dummyUser2))
  private val robot3 = robotRepository.findRobotByUser(dummyUser3) ?: robotRepository.save(Robot(null, Color.randomColor(), 0, dummyUser3))

  private val bots = listOf(WallHuggerBot(gameHandler, robot1), Bot(gameHandler, robot2), ChillerBot(gameHandler, robot3))

  @Scheduled(fixedRate = 1000, timeUnit = TimeUnit.MILLISECONDS)
  fun dummyRobots() {
    bots.forEach { it.doSomething(gameStateService.getCurrentState()) }
  }
}
