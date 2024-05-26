package xyz.poeschl.roborush.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.extensions.Extension
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import xyz.poeschl.roborush.configuration.OpenApiConfig.Companion.VISIBILITY_KEY
import xyz.poeschl.roborush.configuration.OpenApiConfig.Companion.VISIBILITY_PUBLIC
import xyz.poeschl.roborush.controller.restmodels.Move
import xyz.poeschl.roborush.controller.restmodels.Scan
import xyz.poeschl.roborush.exceptions.RobotNotActiveException
import xyz.poeschl.roborush.models.ActiveRobot
import xyz.poeschl.roborush.models.PublicRobot
import xyz.poeschl.roborush.models.ScoreboardEntry
import xyz.poeschl.roborush.security.repository.User
import xyz.poeschl.roborush.service.RobotService

@RestController
@RequestMapping("/robot")
class RobotRestController(private val robotService: RobotService) {
  companion object {
    private val LOGGER = LoggerFactory.getLogger(RobotRestController::class.java)
  }

  @GetMapping("/all/active", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getActiveRobots(): List<PublicRobot> {
    LOGGER.debug("Get all robots")
    return robotService.getActiveRobots()
  }

  @GetMapping("/all/scores", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getScoreboard(): List<ScoreboardEntry> {
    return robotService.getTopRobots()
  }

  @Operation(
    summary = "Retrieves data about your robot. (Only successful when participating in a game)",
    extensions = [Extension(name = VISIBILITY_KEY, properties = [ExtensionProperty(name = VISIBILITY_KEY, value = VISIBILITY_PUBLIC)])]
  )
  @SecurityRequirement(name = "Bearer Authentication")
  @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
  @PreAuthorize("hasRole('${User.ROLE_USER}')")
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
  @PreAuthorize("hasRole('${User.ROLE_USER}')")
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

  @Operation(
    summary = "Tell your robot to wait for the time of the next round. Can only be called during the 'waiting for input' phase.",
    extensions = [Extension(name = VISIBILITY_KEY, properties = [ExtensionProperty(name = VISIBILITY_KEY, value = VISIBILITY_PUBLIC)])]
  )
  @SecurityRequirement(name = "Bearer Authentication")
  @PostMapping("/action/wait")
  fun wait(auth: Authentication) {
    LOGGER.debug("Wait the next move")
    robotService.executeWithActiveRobotIdOfUser(auth.principal as User) {
      robotService.scheduleWait(it)
    }
  }

  @Operation(
    summary = "Refuel your robot, if its standing on a fuel tile. Can only be called during the 'waiting for input' phase and while standing on a fuel tile.",
    extensions = [Extension(name = VISIBILITY_KEY, properties = [ExtensionProperty(name = VISIBILITY_KEY, value = VISIBILITY_PUBLIC)])]
  )
  @SecurityRequirement(name = "Bearer Authentication")
  @PostMapping("/action/refuel")
  fun refuel(auth: Authentication) {
    LOGGER.debug("Refuel robot")
    robotService.executeWithActiveRobotIdOfUser(auth.principal as User) {
      robotService.scheduleRefuel(it)
    }
  }

  @Operation(
    summary = """
      Take the next turn and re-charge a little amount of fuel via solar power. This action is dependent on the map.
      Can only be called during the 'waiting for input' phase.
      """,
    extensions = [Extension(name = VISIBILITY_KEY, properties = [ExtensionProperty(name = VISIBILITY_KEY, value = VISIBILITY_PUBLIC)])]
  )
  @SecurityRequirement(name = "Bearer Authentication")
  @PostMapping("/action/solarCharge")
  fun solarCharge(auth: Authentication) {
    LOGGER.debug("Solar charge robot")
    robotService.executeWithActiveRobotIdOfUser(auth.principal as User) {
      robotService.scheduleSolarCharge(it)
    }
  }
}
