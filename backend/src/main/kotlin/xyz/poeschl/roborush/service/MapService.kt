package xyz.poeschl.roborush.service

import jakarta.transaction.Transactional
import org.hibernate.Hibernate
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import xyz.poeschl.roborush.controller.restmodels.MapAttributeSaveDto
import xyz.poeschl.roborush.models.*
import xyz.poeschl.roborush.repositories.Map
import xyz.poeschl.roborush.repositories.MapRepository
import xyz.poeschl.roborush.repositories.Tile
import xyz.poeschl.roborush.repositories.TileRepository
import kotlin.time.measureTime

@Service
class MapService(private val mapRepository: MapRepository, private val tileRepository: TileRepository) {

  companion object {
    private val LOGGER = LoggerFactory.getLogger(MapService::class.java)
  }

  fun saveMap(map: Map): Map {
    val saved: Map
    val saveDuration = measureTime {
      saved = mapRepository.save(map)
    }

    LOGGER.info("Saved map '{}' ({}x{}) in {} ms", map.mapName, map.size.width, map.size.height, saveDuration.inWholeMilliseconds)
    return saved
  }

  fun getAllMaps(): List<Map> = mapRepository.findAllByOrderById()

  fun getMap(id: Long): Map? = mapRepository.findById(id).orElse(null)

  @Transactional
  fun getNextChallengeMap(): Map {
    val map = mapRepository.findAllByActiveIsTrueOrderById().random()
    // We need something better than this!
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

    map.solarChargeRate =
      when {
        attributes.solarChargeRate < 0 -> 0.0
        attributes.solarChargeRate > 1 -> 1.0
        else -> attributes.solarChargeRate
      }

    return mapRepository.save(map)
  }

  @Transactional
  fun setMapTile(map: Map, tile: Tile): Map {
    val stored = map.mapData.find { it.position == tile.position }
    stored?.let { storedTile ->
      when {
        tile.type == TileType.START_TILE && storedTile.type == TileType.DEFAULT_TILE -> {
          // A start tile is added
          map.possibleStartPositions.add(storedTile.position)
          storedTile.type = TileType.START_TILE
        }

        tile.type == TileType.DEFAULT_TILE && storedTile.type == TileType.START_TILE -> {
          // A start tile is removed
          if (map.possibleStartPositions.size > 1) {
            // Keep at least one start position
            map.possibleStartPositions.remove(storedTile.position)
            storedTile.type = TileType.DEFAULT_TILE
          }
        }

        tile.type == TileType.TARGET_TILE && storedTile.type == TileType.DEFAULT_TILE -> {
          // A new target tile is stored
          val oldTarget = map.mapData.find { it.position == map.targetPosition }
          oldTarget?.let { old ->
            old.type = TileType.DEFAULT_TILE
            tileRepository.save(old)
          }
          map.targetPosition = storedTile.position
          storedTile.type = TileType.TARGET_TILE
        }

        tile.type == TileType.DEFAULT_TILE && storedTile.type == TileType.TARGET_TILE -> {
          // The target is removed, which is not possible
        }
        else -> {
          storedTile.type = tile.type
        }
      }
      tileRepository.save(storedTile)
    }
    return mapRepository.save(map)
  }

  fun deleteMap(map: Map) {
    val deleteMap = measureTime {
      mapRepository.delete(map)
    }

    LOGGER.info("Deleted map '{}' ({}x{}) in {} ms", map.mapName, map.size.width, map.size.height, deleteMap.inWholeMilliseconds)
  }
}
