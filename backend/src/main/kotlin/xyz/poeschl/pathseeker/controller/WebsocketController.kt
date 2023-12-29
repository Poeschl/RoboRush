package xyz.poeschl.pathseeker.controller

import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import xyz.poeschl.pathseeker.models.ActiveRobot
import xyz.poeschl.pathseeker.models.PublicRobot

@Controller
class WebsocketController(private val messageTemplate: SimpMessagingTemplate) {
  fun sendRobotUpdate(robot: ActiveRobot) {
    val publicRobot = PublicRobot(robot.id, robot.color, robot.position)
    messageTemplate.convertAndSend("/topic/robots", publicRobot)
  }
}
