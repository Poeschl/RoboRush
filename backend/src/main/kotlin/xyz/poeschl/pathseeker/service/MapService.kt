package xyz.poeschl.pathseeker.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import xyz.poeschl.pathseeker.exceptions.NoStartingPosition
import xyz.poeschl.pathseeker.exceptions.NoTargetPosition
import xyz.poeschl.pathseeker.exceptions.UnknownTileType
import xyz.poeschl.pathseeker.gamelogic.GameHandler
import xyz.poeschl.pathseeker.models.*
import xyz.poeschl.pathseeker.models.Map
import java.io.InputStream
import javax.imageio.ImageIO
import kotlin.time.measureTime

@Service
class MapService(private val gameHandler: GameHandler) {

  companion object {
    private val LOGGER = LoggerFactory.getLogger(MapService::class.java)
  }

  fun getHeightMap(): List<Tile> {
    return gameHandler.getHeightMap()
  }

  /**
   * Generates a new map object from the given height map image and returns all detected recoverable errors as result.
   *
   * @return A list of detected errors as string list. All of those errors are somehow escaped, but the map might not be ideal.
   */
  fun createNewMapFromHeightMap(heightMapFile: InputStream): InternalMapGenResult {
    LOGGER.info("Create new map from height map image")

    val result: InternalMapGenResult
    val generationDuration = measureTime {
      result = convertHeightImageToMap(heightMapFile)
    }

    LOGGER.info("Created map in ${generationDuration.inWholeMilliseconds} ms")
    return result
  }

  private fun convertHeightImageToMap(heightMapFile: InputStream): InternalMapGenResult {
    val image = ImageIO.read(heightMapFile.buffered())

    val startingPositions = mutableListOf<Position>()
    var targetPosition: Position? = null
    val tiles = mutableListOf<Tile>()
    val errors = mutableListOf<String>()

    for (y in 0..<image.height) {
      for (x in 0..<image.width) {
        val pos = Position(x, y)
        val pixelColor = Color.fromColorInt(image.getRGB(x, y))

        var tileData: TileData
        try {
          tileData = getTileData(pixelColor)
        } catch (ex: UnknownTileType) {
          LOGGER.warn("Unknown tile type detected at ({},{}) with color {}. Inserting default!", pos.x, pos.y, pixelColor.toString())
          errors.add("Unknown tile type detected at (%d,%d) with color (%d, %d, %d).".format(pos.x, pos.y, pixelColor.r, pixelColor.g, pixelColor.b))
          tileData = TileData(0, TileType.DEFAULT_TILE)
        }

        when (tileData.type) {
          TileType.DEFAULT_TILE -> tiles.add(Tile(pos, tileData.height, tileData.type))

          TileType.START_TILE -> {
            tiles.add(Tile(pos, tileData.height, tileData.type))
            startingPositions.add(pos)
            LOGGER.debug("Detected start point at ({},{})", pos.x, pos.y)
          }

          TileType.TARGET_TILE -> {
            if (targetPosition == null) {
              tiles.add(Tile(pos, tileData.height, tileData.type))
              targetPosition = pos
              LOGGER.debug("Detected target point at ({},{})", pos.x, pos.y)
            } else {
              errors.add("Multiple target positions detected. Using first one (%d,%d) and skip all others!".format(pos.x, pos.y))
            }
          }
        }
      }
    }

    if (startingPositions.isEmpty()) {
      LOGGER.warn("No starting position detected")
      throw NoStartingPosition("At least one starting position is required")
    }

    if (targetPosition == null) {
      LOGGER.warn("No target position detected")
      throw NoTargetPosition("At least one target position is required")
    }

    val mapData = Array(tiles.size) { tiles[it] }

    return InternalMapGenResult(Map(Size(image.width, image.height), mapData, startingPositions, targetPosition), errors)
  }

  private fun getTileData(color: Color): TileData {
    return if (color.isGrey()) {
      val height = color.r
      TileData(height, TileType.DEFAULT_TILE)
    } else if (color.g > color.r && color.r == color.b) {
      // starting points
      val height = color.r
      TileData(height, TileType.START_TILE)
    } else if (color.r > color.g && color.g == color.b) {
      // target points
      val height = color.g
      TileData(height, TileType.TARGET_TILE)
    } else {
      throw UnknownTileType("Unknown tile type detected")
    }
  }

  private data class TileData(val height: Int, val type: TileType)
}
