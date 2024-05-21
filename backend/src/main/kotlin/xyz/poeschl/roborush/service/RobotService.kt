package xyz.poeschl.roborush.service

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import xyz.poeschl.roborush.exceptions.RobotNotActiveException
import xyz.poeschl.roborush.gamelogic.GameHandler
import xyz.poeschl.roborush.gamelogic.actions.*
import xyz.poeschl.roborush.models.ActiveRobot
import xyz.poeschl.roborush.models.Direction
import xyz.poeschl.roborush.models.Position
import xyz.poeschl.roborush.models.PublicRobot
import xyz.poeschl.roborush.models.ScoreboardEntry
import xyz.poeschl.roborush.repositories.Robot
import xyz.poeschl.roborush.repositories.RobotRepository
import xyz.poeschl.roborush.security.repository.User

@Service
class RobotService(private val robotRepository: RobotRepository, private val gameHandler: GameHandler) {

  fun getRobotByUser(user: User): Robot? {
    return robotRepository.findRobotByUser(user)
  }

  @Cacheable("userRobotCache", key = "#user.username")
  fun getActiveRobotByUser(user: User): ActiveRobot? {
    robotRepository.findRobotByUser(user)?.let {
      return gameHandler.getActiveRobot(it.id!!)
    }
    return null
  }

  fun executeWithActiveRobotIdOfUser(user: User, action: (robotId: Long) -> Unit) {
    val activeRobot = getActiveRobotByUser(user)
    if (activeRobot != null) {
      action(activeRobot.id)
    } else {
      throw RobotNotActiveException("No active Robot found")
    }
  }

  fun getActiveRobots(): List<PublicRobot> {
    return gameHandler.getActiveRobots()
      .map { PublicRobot(it.id, it.name, it.color, it.position) }
      .sortedBy(PublicRobot::id)
  }

  fun registerRobotForGame(robotId: Long) {
    gameHandler.registerRobotForNextGame(robotId)
  }

  fun scheduleScan(robotId: Long, distance: Int) {
    gameHandler.nextActionForRobot(robotId, ScanAction(distance))
  }

  fun scheduleMove(robotId: Long, direction: Direction) {
    gameHandler.nextActionForRobot(robotId, MoveAction(direction))
  }

  fun scheduleWait(robotId: Long) {
    gameHandler.nextActionForRobot(robotId, WaitAction())
  }

  fun scheduleRefuel(robotId: Long) {
    gameHandler.nextActionForRobot(robotId, RefuelAction())
  }

  fun scheduleSolarCharge(robotId: Long) {
    gameHandler.nextActionForRobot(robotId, SolarChargeAction())
  }

  fun getTopRobots(): List<ScoreboardEntry> {
    return robotRepository.findTop10Robots()
      .map { ScoreboardEntry(it.user.username, it.color, it.score) }
  }

  fun getKnownPositionsForRobot(robotId: Long): Set<Position> {
    return gameHandler.getActiveRobot(robotId)?.knownPositions ?: throw RobotNotActiveException("No active Robot found")
  }

  fun getKnownPositionsForAllRobots(): Set<Position> {
    return gameHandler.getActiveRobots().map { it.knownPositions }.flatten().toSet()
  }
}
