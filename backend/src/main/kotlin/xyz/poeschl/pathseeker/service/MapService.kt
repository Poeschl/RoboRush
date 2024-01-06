package xyz.poeschl.pathseeker.service

import org.springframework.stereotype.Service
import xyz.poeschl.pathseeker.gamelogic.GameHandler
import xyz.poeschl.pathseeker.models.Tile

@Service
class MapService(private val gameHandler: GameHandler) {

  fun getHeightMap(): List<Tile> {
    return gameHandler.getHeightMap()
  }
}
