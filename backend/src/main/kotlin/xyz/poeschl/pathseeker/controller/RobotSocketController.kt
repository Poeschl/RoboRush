package xyz.poeschl.pathseeker.controller

import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller
import xyz.poeschl.pathseeker.models.Robot

@Controller
class RobotSocketController {

  @SendTo("/app/robots")
  fun sendRobotInformation(robots: List<Robot>): List<Robot> {
    return robots
  }
}
