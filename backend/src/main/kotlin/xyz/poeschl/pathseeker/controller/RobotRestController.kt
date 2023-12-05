package xyz.poeschl.pathseeker.controller

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import xyz.poeschl.pathseeker.controller.restmodels.RobotMove
import xyz.poeschl.pathseeker.models.Position
import xyz.poeschl.pathseeker.models.PublicRobot
import xyz.poeschl.pathseeker.models.Robot
import xyz.poeschl.pathseeker.models.Tile
import xyz.poeschl.pathseeker.service.RobotService

@RestController
@RequestMapping("/robot")
class RobotRestController(private val robotService: RobotService) {
  companion object {
    private val LOGGER = LoggerFactory.getLogger(RobotRestController::class.java)
  }

  @GetMapping("/all", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getAllRobots(): List<PublicRobot> {
    LOGGER.debug("Get all robots")
    return robotService.getAllRobots().map { PublicRobot(it.id, it.color, it.position) }
  }

  @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getRobot(@RequestParam robotId: Int): Robot {
    LOGGER.debug("Get robot")
    return robotService.getRobot(robotId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @GetMapping("/scan", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getScanData(@RequestParam robotId: Int, @RequestParam distance: Int): List<Tile>? {
    LOGGER.debug("Called scan")
    return robotService.getRobot(robotId)?.let { robotService.scan(it, distance) }
  }

  @PostMapping("/move", produces = [MediaType.APPLICATION_JSON_VALUE], consumes = [MediaType.APPLICATION_JSON_VALUE])
  fun moveInDirection(@RequestBody robotMove: RobotMove): Position? {
    LOGGER.debug("Move robot")
    return robotService.getRobot(robotMove.robotIndex)?.let { robotService.move(it, robotMove.direction) }
  }
}
