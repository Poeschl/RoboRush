package xyz.poeschl.pathseeker.gamelogic.internal

import org.slf4j.LoggerFactory
import xyz.poeschl.pathseeker.configuration.GameLogic
import xyz.poeschl.pathseeker.models.*
import xyz.poeschl.pathseeker.models.Map
import java.util.stream.IntStream
import kotlin.math.ceil
import kotlin.random.Random
import kotlin.streams.toList

@GameLogic
class MapHandler {
  private var currentMap = Map(size = Size(0, 0), arrayOf(emptyArray()), listOf(), Position(0, 0))

  companion object {
    private val LOGGER = LoggerFactory.getLogger(MapHandler::class.java)
    private const val STATIC_FUEL_COST = 1
    private const val TILE_SCAN_COST = 0.15
  }

  fun getHeightMap(): List<Tile> {
    return currentMap.mapData.flatMap { it.toList() }
  }

  fun getStartPositions(): List<Position> {
    return currentMap.possibleStartPositions
  }

  fun getTargetPosition(): Position {
    return currentMap.targetPosition
  }

  fun createNewRandomMap(size: Size) {
    LOGGER.info("Create new random map ({}x{})", size.width, size.height)

    val randomHeights = IntStream.range(0, size.width * size.height)
      .map { Random.nextInt(0, 8) }.toList()
    val startPositions = listOf(Position(0, 0), Position(0, 1), Position(1, 0), Position(1, 1))
    val targetPosition = Position(size.width - 2, size.height - 2)
    currentMap = Map(size, createHeightMap(size, randomHeights, startPositions, targetPosition), startPositions, targetPosition)
  }

  fun createNewPresetMap(size: Size, heights: List<Int>, start: Position, target: Position = Position(size.width - 1, size.height - 1)) {
    LOGGER.info("Create static map with heights ${heights.joinToString(", ")}")
    currentMap = Map(size, createHeightMap(size, heights, listOf(start), target), listOf(start), target)
  }

  /**
   * Creates a new map with the heights given as a list.
   *
   * @param size The map size
   * @param heights The heights of the map as list. It continues horizontally.
   */
  private fun createHeightMap(size: Size, heights: List<Int>, startPositions: List<Position>, target: Position): Array<Array<Tile>> {
    val rows = mutableListOf<List<Tile>>()
    for (y in 0..<size.height) {
      val row = mutableListOf<Tile>()
      for (x in 0..<size.width) {
        val pos = Position(x, y)

        val type =
          if (target == pos) {
            TileType.TARGET_TILE
          } else if (startPositions.contains(pos)) {
            TileType.START_TILE
          } else {
            TileType.DEFAULT_TILE
          }

        row.add(Tile(pos, heights[(y * size.width) + x], type))
      }
      rows.add(row)
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
    return currentMap.mapData[position.y][position.x]
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
          list.add(currentMap.mapData[y][x])
        }
        usedFuel += TILE_SCAN_COST
      }
    }
    return Pair(list, ceil(usedFuel).toInt())
  }
}
