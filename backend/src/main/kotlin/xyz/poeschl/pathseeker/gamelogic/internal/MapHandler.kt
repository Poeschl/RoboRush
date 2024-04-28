package xyz.poeschl.pathseeker.gamelogic.internal

import org.slf4j.LoggerFactory
import xyz.poeschl.pathseeker.configuration.GameLogic
import xyz.poeschl.pathseeker.models.Position
import xyz.poeschl.pathseeker.models.Size
import xyz.poeschl.pathseeker.repositories.Map
import xyz.poeschl.pathseeker.repositories.Tile
import kotlin.math.ceil

@GameLogic
class MapHandler {
  private var currentMap = Map(0, "init", Size(0, 0), listOf(), Position(0, 0), 0)

  // Use a 2D array for faster tile data access
  private var currentMapTiles = arrayOf(emptyArray<Tile>())

  companion object {
    private val LOGGER = LoggerFactory.getLogger(MapHandler::class.java)
    private const val STATIC_FUEL_COST = 1
    private const val TILE_SCAN_COST = 0.15
  }

  fun getCurrentMap(): Map {
    return currentMap
  }

  fun getStartPositions(): List<Position> {
    return currentMap.possibleStartPositions
  }

  fun getTargetPosition(): Position {
    return currentMap.targetPosition
  }

  fun getRobotMaxFuel(): Int {
    return currentMap.maxRobotFuel
  }

  fun loadNewMap(map: Map) {
    currentMap = map

    currentMapTiles = Array(map.size.height) { y ->
      Array(map.size.width) { x ->
        map.mapData.first { it.position == Position(x, y) }
      }
    }
  }

  fun isPositionValid(position: Position): Boolean {
    return (0 <= position.x) && (position.x < currentMap.size.width) && (0 <= position.y) && (position.y < currentMap.size.height)
  }

  fun getTileAtPosition(position: Position): Tile {
    return currentMapTiles[position.y][position.x]
  }

  fun getFuelCost(oldPosition: Position, newPosition: Position): Int {
    // Rolling down a hill cost no extra fuel
    return STATIC_FUEL_COST + (getTileAtPosition(newPosition).height - getTileAtPosition(oldPosition).height).coerceAtLeast(0)
  }

  /**
   * Get all tiles in range of manhattan distance around the position.
   *
   * @return A pair of all tiles and the cost for the taken scan.
   */
  fun getTilesInDistance(position: Position, distance: Int): Pair<List<Tile>, Int> {
    var usedFuel = 0.0
    val list = mutableListOf<Tile>()
    for (y in (position.y - distance)..position.y + distance) {
      for (x in (position.x - distance)..position.x + distance) {
        val checked = Position(x, y)
        if (isPositionValid(checked) && checked.getDistanceTo(position) <= distance) {
          list.add(currentMapTiles[y][x])
        }
        usedFuel += TILE_SCAN_COST
      }
    }
    return Pair(list, ceil(usedFuel).toInt())
  }

  fun isSolarChargePossible(): Boolean {
    return currentMap.solarChargeRate > 0
  }

  fun getSolarChargeRate(): Double {
    return currentMap.solarChargeRate
  }
}
