package xyz.poeschl.pathseeker.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import xyz.poeschl.pathseeker.models.Position
import xyz.poeschl.pathseeker.models.Size

@Service
class GameService(private val mapService: MapService, private val robotService: RobotService) {
  companion object {
    private val LOGGER = LoggerFactory.getLogger(GameService::class.java)
  }

  fun startGame() {
    // Playfield init
    mapService.createNewRandomMap(Size(16, 8))
    var map: String = mapService.debugMap()
    LOGGER.debug("\nInitial map:\n{}", map)

    // Robot 1
    val robot1 = robotService.createAndStoreRobot(100, Position(5, 7))
    // Robot 2
    val robot2 = robotService.createAndStoreRobot(100, Position(2, 3))

    val robot1pos = robot1.position.x + (mapService.currentMap.size.width + 1) * robot1.position.y
    val robot2pos = robot2.position.x + (mapService.currentMap.size.width + 1) * robot2.position.y
    var robotMap = map.replaceRange(robot1pos, robot1pos + 1, "R")
    robotMap = robotMap.replaceRange(robot2pos, robot2pos + 1, "R")
    LOGGER.debug("\nMap with robots:\n{}", robotMap)

    // Turns
  }
}
