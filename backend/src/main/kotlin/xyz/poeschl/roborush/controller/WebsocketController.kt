package xyz.poeschl.roborush.controller

import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import xyz.poeschl.roborush.gamelogic.GameState
import xyz.poeschl.roborush.models.ActiveRobot
import xyz.poeschl.roborush.models.PublicRobot
import xyz.poeschl.roborush.models.settings.ClientSettings
import xyz.poeschl.roborush.repositories.Map

@Controller
class WebsocketController(private val messageTemplate: SimpMessagingTemplate) {

  fun sendRobotUpdate(robot: ActiveRobot) {
    messageTemplate.convertAndSend("/topic/robot", PublicRobot(robot.id, robot.name, robot.color, robot.position))
  }

  fun sendUserRobotData(robot: ActiveRobot) {
    val user = robot.user.username
    messageTemplate.convertAndSendToUser(user, "/queue/robot", robot)
  }

  fun sendGameStateUpdate(gameState: GameState) {
    messageTemplate.convertAndSend("/topic/game/state", gameState)
  }

  fun sendTurnUpdate(turn: Int) {
    messageTemplate.convertAndSend("/topic/game/turn", turn)
  }

  fun sendClientSettingsUpdate(settings: ClientSettings) {
    messageTemplate.convertAndSend("/topic/config/client", settings)
  }

  fun sendKnownPositionsUpdate(robot: ActiveRobot) {
    val user = robot.user.username
    messageTemplate.convertAndSendToUser(user, "/queue/robot/knownPositions", robot.knownPositions)
  }

  fun sendMapTileUpdate(map: Map) {
    messageTemplate.convertAndSend("/topic/map/tiles", map.mapData)
  }
}
