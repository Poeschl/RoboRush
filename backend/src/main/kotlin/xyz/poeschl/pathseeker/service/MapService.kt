package xyz.poeschl.pathseeker.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import xyz.poeschl.pathseeker.gamelogic.GameHandler
import xyz.poeschl.pathseeker.models.Tile
import java.io.InputStream

@Service
class MapService(private val gameHandler: GameHandler) {

  companion object {
    private val LOGGER = LoggerFactory.getLogger(MapService::class.java)
  }

  fun getHeightMap(): List<Tile> {
    return gameHandler.getHeightMap()
  }

  fun newMapFromHeightMap(name: String, heightMapFile: InputStream) {
    LOGGER.info("Process new height map '{}'", name)
  }
}
