package xyz.poeschl.pathseeker.service

import jakarta.transaction.Transactional
import org.hibernate.Hibernate
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import xyz.poeschl.pathseeker.controller.restmodels.MapAttributeSaveDto
import xyz.poeschl.pathseeker.exceptions.NoStartingPosition
import xyz.poeschl.pathseeker.exceptions.NoTargetPosition
import xyz.poeschl.pathseeker.exceptions.UnknownTileType
import xyz.poeschl.pathseeker.models.*
import xyz.poeschl.pathseeker.repositories.Map
import xyz.poeschl.pathseeker.repositories.MapRepository
import xyz.poeschl.pathseeker.repositories.Tile
import java.io.InputStream
import javax.imageio.ImageIO
import kotlin.time.measureTime

@Service
class MapService(private val mapRepository: MapRepository) {

  companion object {
    private val LOGGER = LoggerFactory.getLogger(MapService::class.java)
    private val DEFAULT_MAP_FUEL = 300
  }

  fun saveMap(map: Map): Map {
    val saved: Map
    val saveDuration = measureTime {
      saved = mapRepository.save(map)
    }

    LOGGER.info("Saved map '{}' ({}x{}) in {} ms", map.mapName, map.size.width, map.size.height, saveDuration.inWholeMilliseconds)
    return saved
  }

  fun getAllMaps(): List<Map> {
    return mapRepository.findAllByOrderById()
  }

  fun getMap(id: Long): Map? {
    return mapRepository.findById(id).orElse(null)
  }

  @Transactional
  fun getNextChallengeMap(): Map {
    val map = mapRepository.findAllByActiveIsTrueOrderById().random()
    Hibernate.initialize(map.mapData)
    return map
  }

  fun setMapActive(map: Map, active: Boolean): Map {
    map.active = active
    return mapRepository.save(map)
  }

  fun setMapAttributes(map: Map, attributes: MapAttributeSaveDto): Map {
    map.mapName = attributes.mapName
    map.maxRobotFuel = attributes.maxRobotFuel
    return mapRepository.save(map)
  }

  fun deleteMap(map: Map) {
    val deleteMap = measureTime {
      mapRepository.delete(map)
    }

    LOGGER.info("Deleted map '{}' ({}x{}) in {} ms", map.mapName, map.size.width, map.size.height, deleteMap.inWholeMilliseconds)
  }

  /**
   * Generates a new map object from the given height map image and returns all detected recoverable errors as result.
   *
   * @return A list of detected errors as string list. All of those errors are somehow escaped, but the map might not be ideal.
   */
  fun createNewMapFromHeightMap(mapName: String, heightMapFile: InputStream): InternalMapGenResult {
    LOGGER.info("Create new map from height map image")

    val result: InternalMapGenResult
    val generationDuration = measureTime {
      result = convertHeightImageToMap(mapName, heightMapFile)
    }

    LOGGER.info("Created map '{}' ({}x{}) in {} ms", result.map.mapName, result.map.size.width, result.map.size.height, generationDuration.inWholeMilliseconds)
    return result
  }

  private fun convertHeightImageToMap(mapName: String, heightMapFile: InputStream): InternalMapGenResult {
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
          TileType.DEFAULT_TILE -> tiles.add(Tile(null, pos, tileData.height, tileData.type))
          TileType.FUEL_TILE -> tiles.add(Tile(null, pos, tileData.height, tileData.type))

          TileType.START_TILE -> {
            tiles.add(Tile(null, pos, tileData.height, tileData.type))
            startingPositions.add(pos)
            LOGGER.debug("Detected start point at ({},{})", pos.x, pos.y)
          }

          TileType.TARGET_TILE -> {
            if (targetPosition == null) {
              tiles.add(Tile(null, pos, tileData.height, tileData.type))
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

    val map = Map(null, mapName, Size(image.width, image.height), startingPositions, targetPosition, DEFAULT_MAP_FUEL)
    // Add all tiles to map for the db relations
    tiles.forEach { map.addTile(it) }

    return InternalMapGenResult(map, errors)
  }

  private fun getTileData(color: Color): TileData {
    return when {
      color.isGrey() -> {
        val height = color.r
        TileData(height, TileType.DEFAULT_TILE)
      }

      color.g > color.r && color.r == color.b -> {
        // starting points
        val height = color.r
        TileData(height, TileType.START_TILE)
      }

      color.r > color.g && color.g == color.b -> {
        // target points
        val height = color.g
        TileData(height, TileType.TARGET_TILE)
      }

      color.b > color.r && color.g == color.r -> {
        val height = color.r
        TileData(height, TileType.FUEL_TILE)
      }
      else -> {
        throw UnknownTileType("Unknown tile type detected")
      }
    }
  }

  private data class TileData(val height: Int, val type: TileType)
}
