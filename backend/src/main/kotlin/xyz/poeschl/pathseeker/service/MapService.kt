package xyz.poeschl.pathseeker.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import xyz.poeschl.pathseeker.models.Map
import xyz.poeschl.pathseeker.models.Position
import xyz.poeschl.pathseeker.models.Size
import xyz.poeschl.pathseeker.models.Tile
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.random.Random

@Service
class MapService {
  var currentMap = Map(size = Size(0, 0), heightMap = arrayOf(emptyArray()))

  companion object {
    private val LOGGER = LoggerFactory.getLogger(MapService::class.java)
    private const val STATIC_FUEL_COST = 1
    private const val TILE_SCAN_COST = 0.15
  }

  fun createNewMap(size: Size) {
    LOGGER.info("Create new map ({}x{})", size.width, size.height)

    currentMap = Map(size, createRandomHeightMap(size))
  }

  fun debugMap(): String {
    var area = ""
    for (y in 0..<currentMap.size.height) {
      var row = ""
      for (x in 0..<currentMap.size.width) {
        row += currentMap.heightMap[x][y].height.toString()
      }
      area += row + "\n"
    }

    return area
  }

  private fun createRandomHeightMap(size: Size): Array<Array<Tile>> {
    val rows = mutableListOf<List<Tile>>()
    for (x in 0..<size.width) {
      val column = mutableListOf<Tile>()
      for (y in 0..<size.height) {
        column.add(Tile(Position(x, y), Random.nextInt(0, 8)))
      }
      rows.add(column)
    }

    val rowArray =
      Array(rows.size) {
        rows[it].toTypedArray()
      }

    return rowArray
  }

  fun isPositionValid(position: Position): Boolean {
    return (0 <= position.x) && (position.x < currentMap.size.width) && (0 <= position.y) && (position.y < currentMap.size.height)
  }

  fun getTileAtPosition(position: Position): Tile {
    return currentMap.heightMap[position.x][position.y]
  }

  fun getFuelCost(oldPosition: Position, newPosition: Position): Int {
    return STATIC_FUEL_COST + abs(getTileAtPosition(oldPosition).height - getTileAtPosition(newPosition).height)
  }

  /***
   * Get all tiles in range of manhattan distance around the position.
   *
   * @return A pair of all tiles and the cost for the taken scan.
   */
  fun getTilesInDistance(position: Position, distance: Int): Pair<MutableList<Tile>, Int> {
    var usedFuel = 0.0
    val list = mutableListOf<Tile>()
    for (x in (position.x - distance)..position.x + distance) {
      for (y in (position.y - distance)..position.y + distance) {
        val checked = Position(x, y)
        if (isPositionValid(checked) && checked.getDistanceTo(position) <= distance) {
          list.add(currentMap.heightMap[x][y])
        }
        usedFuel += TILE_SCAN_COST
      }
    }
    return Pair(list, ceil(usedFuel).toInt())
  }
}
