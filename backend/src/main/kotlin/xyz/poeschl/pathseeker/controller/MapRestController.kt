package xyz.poeschl.pathseeker.controller

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import xyz.poeschl.pathseeker.models.Tile
import xyz.poeschl.pathseeker.service.MapService

@RestController
@RequestMapping("/map")
@SecurityRequirement(name = "Bearer Authentication")
class MapRestController(private val mapService: MapService) {
  companion object {
    private val LOGGER = LoggerFactory.getLogger(MapRestController::class.java)
  }

  @GetMapping("/heights", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getHeightMap(): List<Tile> {
    return mapService.currentMap.heightMap.flatMap { it.asList() }
  }
}
