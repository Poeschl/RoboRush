package xyz.poeschl.roborush.gamelogic.dummybots

import org.springframework.scheduling.annotation.Scheduled
import xyz.poeschl.roborush.configuration.GameLogic
import xyz.poeschl.roborush.gamelogic.GameHandler
import xyz.poeschl.roborush.models.Color
import xyz.poeschl.roborush.models.settings.SettingKey
import xyz.poeschl.roborush.repositories.Robot
import xyz.poeschl.roborush.repositories.RobotRepository
import xyz.poeschl.roborush.security.repository.User
import xyz.poeschl.roborush.security.repository.UserRepository
import xyz.poeschl.roborush.service.ConfigService
import java.util.concurrent.TimeUnit

@GameLogic
class DummyBots(
  private val gameHandler: GameHandler,
  private val robotRepository: RobotRepository,
  private val userRepository: UserRepository,
  private val configService: ConfigService
) {

  companion object {
    // Pass: 12345678
    private const val DUMMY_PASSWORD = "\$2a\$10\$G/P5WCSp0c4YSq9wFrdxBOUEZU4aPSmfvs/Ev4jxIT/r02v2/FRii"
  }

  private val robot1 = createOrGetRobot("dummy-wall")
  private val robot2 = createOrGetRobot("dummy-random")
  private val robot3 = createOrGetRobot("dummy-chill")
  private val robot4 = createOrGetRobot("dummy-target")

  private val bots =
    listOf(WallHuggerBot(gameHandler, robot1), Bot(gameHandler, robot2), ChillerBot(gameHandler, robot3), TargetBot(gameHandler, robot4))

  init {
    bots.forEach { gameHandler.addRobotToIgnoredWinningList(it.getId()!!) }
  }

  @Scheduled(fixedRate = 1000, timeUnit = TimeUnit.MILLISECONDS)
  fun dummyRobots() {
    if (configService.getBooleanSetting(SettingKey.ENABLE_DUMMY_ROBOTS).value) {
      bots.forEach { it.doSomething(gameHandler.getPublicGameInfo().currentState) }
    }
  }

  fun isDummyRobot(robot: Robot?): Boolean = bots.any { it.getId() == robot?.id }

  private fun createOrGetRobot(username: String): Robot {
    val user: User = userRepository.findByUsername(username) ?: userRepository.save(User(null, username, DUMMY_PASSWORD))
    return robotRepository.findRobotByUser(user) ?: robotRepository.save(Robot(null, Color.randomColor(), user))
  }
}
