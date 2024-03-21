package xyz.poeschl.pathseeker.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.extensions.Extension
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import xyz.poeschl.pathseeker.configuration.OpenApiConfig.Companion.VISIBILITY_KEY
import xyz.poeschl.pathseeker.configuration.OpenApiConfig.Companion.VISIBILITY_PUBLIC
import xyz.poeschl.pathseeker.controller.restmodels.Move
import xyz.poeschl.pathseeker.controller.restmodels.Scan
import xyz.poeschl.pathseeker.exceptions.RobotNotActiveException
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

  @Operation(
    summary = "Retrieves data about your robot. (Only successful when participating in a game)",
    extensions = [Extension(name = VISIBILITY_KEY, properties = [ExtensionProperty(name = VISIBILITY_KEY, value = VISIBILITY_PUBLIC)])]
  )
  @SecurityRequirement(name = "Bearer Authentication")
  @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getActiveUserRobot(auth: Authentication): ActiveRobot {
    LOGGER.debug("Get active user robot")
    return robotService.getActiveRobotByUser(auth.principal as User) ?: throw RobotNotActiveException("Your robot is not active right now")
  }

  @Operation(
    summary = "Call this to register your robot for the next game. Can only be called during the game preparation phase.",
    extensions = [Extension(name = VISIBILITY_KEY, properties = [ExtensionProperty(name = VISIBILITY_KEY, value = VISIBILITY_PUBLIC)])]
  )
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

  @Operation(
    summary = "Tell your robot to scan the area around it in the next round. Can only be called during the 'waiting for input' phase.",
    extensions = [Extension(name = VISIBILITY_KEY, properties = [ExtensionProperty(name = VISIBILITY_KEY, value = VISIBILITY_PUBLIC)])]
  )
  @SecurityRequirement(name = "Bearer Authentication")
  @PostMapping("/action/scan", consumes = [MediaType.APPLICATION_JSON_VALUE])
  fun scan(auth: Authentication, @RequestBody scan: Scan) {
    LOGGER.debug("Called scan")
    robotService.executeWithActiveRobotIdOfUser(auth.principal as User) {
      robotService.scheduleScan(it, scan.distance)
    }
  }

  @Operation(
    summary = "Tell your robot to move in a direction in the next round. Can only be called during the 'waiting for input' phase.",
    extensions = [Extension(name = VISIBILITY_KEY, properties = [ExtensionProperty(name = VISIBILITY_KEY, value = VISIBILITY_PUBLIC)])]
  )
  @SecurityRequirement(name = "Bearer Authentication")
  @PostMapping("/action/move", consumes = [MediaType.APPLICATION_JSON_VALUE])
  fun moveInDirection(auth: Authentication, @RequestBody move: Move) {
    LOGGER.debug("Set next robot move")
    robotService.executeWithActiveRobotIdOfUser(auth.principal as User) {
      robotService.scheduleMove(it, move.direction)
    }
  }
}
