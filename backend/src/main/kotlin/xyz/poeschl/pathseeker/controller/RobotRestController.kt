package xyz.poeschl.pathseeker.controller

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import xyz.poeschl.pathseeker.controller.restmodels.Move
import xyz.poeschl.pathseeker.controller.restmodels.Scan
import xyz.poeschl.pathseeker.models.ActiveRobot
import xyz.poeschl.pathseeker.models.PublicRobot
import xyz.poeschl.pathseeker.security.repository.User
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
  fun getRobot(auth: Authentication): ActiveRobot {
    LOGGER.debug("Get robot")
    return robotService.getActiveRobotByUser(auth.principal as User) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @Tag(name = "public")
  @SecurityRequirement(name = "Bearer Authentication")
  @PostMapping("/attend")
  fun registerRobotForGame(auth: Authentication) {
    val robot = robotService.getRobotByUser(auth.principal as User)
    if (robot != null) {
      robotService.registerRobotForGame(robot.id!!)
    } else {
      throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }
  }

  @Tag(name = "public")
  @SecurityRequirement(name = "Bearer Authentication")
  @PostMapping("/action/scan", consumes = [MediaType.APPLICATION_JSON_VALUE])
  fun getScanData(auth: Authentication, @RequestParam scan: Scan) {
    LOGGER.debug("Called scan")
    robotService.executeWithActiveRobotIdOfUser(auth.principal as User) {
      robotService.scheduleScan(it, scan.distance)
    }
  }

  @Tag(name = "public")
  @SecurityRequirement(name = "Bearer Authentication")
  @PostMapping("/action/move", consumes = [MediaType.APPLICATION_JSON_VALUE])
  fun moveInDirection(auth: Authentication, @RequestBody move: Move) {
    LOGGER.debug("Move robot")
    robotService.executeWithActiveRobotIdOfUser(auth.principal as User) {
      robotService.scheduleMove(it, move.direction)
    }
  }
}
