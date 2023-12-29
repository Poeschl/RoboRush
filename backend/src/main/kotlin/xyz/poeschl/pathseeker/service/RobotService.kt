package xyz.poeschl.pathseeker.service

import org.springframework.stereotype.Service
import xyz.poeschl.pathseeker.gamelogic.GameHandler
import xyz.poeschl.pathseeker.gamelogic.actions.Move
import xyz.poeschl.pathseeker.gamelogic.actions.Scan
import xyz.poeschl.pathseeker.models.ActiveRobot
import xyz.poeschl.pathseeker.models.Direction
import xyz.poeschl.pathseeker.models.PublicRobot

@Service
class RobotService(private val gameHandler: GameHandler) {

  fun getActiveRobots(): List<PublicRobot> {
    return gameHandler.getActiveRobots().map { PublicRobot(it.id, it.color, it.position) }
  }

  fun getActiveRobot(robotId: Long): ActiveRobot? {
    return gameHandler.getActiveRobot(robotId)
  }

  fun scheduleScan(robotId: Long, distance: Int) {
    return gameHandler.nextActionForRobot(robotId, Scan(distance))
  }

  fun scheduleMove(robotId: Long, direction: Direction) {
    return gameHandler.nextActionForRobot(robotId, Move(direction))
  }
}
