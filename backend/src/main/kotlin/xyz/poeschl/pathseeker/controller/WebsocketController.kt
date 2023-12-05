package xyz.poeschl.pathseeker.controller

import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import xyz.poeschl.pathseeker.models.PublicRobot
import xyz.poeschl.pathseeker.models.Robot

@Controller
class WebsocketController(private val messageTemplate: SimpMessagingTemplate) {
  fun sendRobotMoveUpdate(robot: Robot) {
    val publicRobot = PublicRobot(robot.id, robot.color, robot.position)
    messageTemplate.convertAndSend("/topic/robots", publicRobot)
  }
}
