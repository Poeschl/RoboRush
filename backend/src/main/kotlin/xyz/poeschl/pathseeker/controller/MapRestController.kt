package xyz.poeschl.pathseeker.controller

import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import xyz.poeschl.pathseeker.repositories.Tile
import xyz.poeschl.pathseeker.service.MapService
import xyz.poeschl.pathseeker.service.RobotService

@RestController
@RequestMapping("/map")
class MapRestController(private val mapService: MapService, private val robotService: RobotService) {
  companion object {
    private val LOGGER = LoggerFactory.getLogger(MapRestController::class.java)
  }

  @GetMapping("/heights", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getHeightMap(): List<Tile> {
    return mapService.getHeightMap()
  }
}
