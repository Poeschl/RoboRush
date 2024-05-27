package xyz.poeschl.roborush.controller

import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import xyz.poeschl.roborush.gamelogic.GameState
import xyz.poeschl.roborush.models.ActiveRobot
import xyz.poeschl.roborush.models.PublicRobot
import xyz.poeschl.roborush.models.settings.ClientSettings

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
}
