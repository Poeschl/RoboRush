package xyz.poeschl.pathseeker.controller

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import xyz.poeschl.pathseeker.controller.restmodels.RobotMove
import xyz.poeschl.pathseeker.models.ActiveRobot
import xyz.poeschl.pathseeker.models.PublicRobot
import xyz.poeschl.pathseeker.service.RobotService

@RestController
@RequestMapping("/robot")
class RobotRestController(private val robotService: RobotService) {
  companion object {
    private val LOGGER = LoggerFactory.getLogger(RobotRestController::class.java)
  }

  @GetMapping("/all", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getActiveRobots(): List<PublicRobot> {
    LOGGER.debug("Get all robots")
    return robotService.getActiveRobots()
  }

  @Tag(name = "public")
  @SecurityRequirement(name = "Bearer Authentication")
  @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getRobot(@RequestParam robotId: Long): ActiveRobot {
    LOGGER.debug("Get robot")
    return robotService.getActiveRobot(robotId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @Tag(name = "public")
  @SecurityRequirement(name = "Bearer Authentication")
  @GetMapping("/scan", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getScanData(@RequestParam robotId: Long, @RequestParam distance: Int) {
    LOGGER.debug("Called scan")
    robotService.getActiveRobot(robotId)?.let { robotService.scheduleScan(it.id, distance) }
  }

  @Tag(name = "public")
  @SecurityRequirement(name = "Bearer Authentication")
  @PostMapping("/move", produces = [MediaType.APPLICATION_JSON_VALUE], consumes = [MediaType.APPLICATION_JSON_VALUE])
  fun moveInDirection(@RequestBody robotMove: RobotMove) {
    LOGGER.debug("Move robot")
    robotService.getActiveRobot(robotMove.robotId)?.let { robotService.scheduleMove(it.id, robotMove.direction) }
  }
}
