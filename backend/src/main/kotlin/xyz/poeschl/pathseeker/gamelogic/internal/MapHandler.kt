package xyz.poeschl.pathseeker.gamelogic.internal

import org.slf4j.LoggerFactory
import xyz.poeschl.pathseeker.configuration.GameLogic
import xyz.poeschl.pathseeker.models.Map
import xyz.poeschl.pathseeker.models.Position
import xyz.poeschl.pathseeker.models.Size
import xyz.poeschl.pathseeker.models.Tile
import java.util.stream.IntStream
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.random.Random
import kotlin.streams.toList

@GameLogic
class MapHandler {
  private var currentMap = Map(size = Size(0, 0), arrayOf(emptyArray()), listOf())

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

  fun createNewRandomMap(size: Size) {
    LOGGER.info("Create new random map ({}x{})", size.width, size.height)

    val randomHeights = IntStream.range(0, size.width * size.height)
      .map { Random.nextInt(0, 8) }.toList()
    currentMap = Map(size, createHeightMap(size, randomHeights), listOf(Position(0, 0), Position(0, 1), Position(1, 0), Position(1, 1)))
  }

  fun createNewPresetMap(size: Size, heights: List<Int>, start: Position) {
    LOGGER.info("Create static map with heights ${heights.joinToString(", ")}")
    currentMap = Map(size, createHeightMap(size, heights), listOf(start))
  }

  fun debugMap(): String {
    var area = ""
    for (y in 0..<currentMap.size.height) {
      var row = ""
      for (x in 0..<currentMap.size.width) {
        row += currentMap.mapData[x][y].height.toString()
      }
      area += row + "\n"
    }

    return area
  }

  /***
   * Creates a new map with the heights given as a list.
   *
   * @param size The map size
   * @param heights The heights of the map as list. It continues horizontally.
   */
  private fun createHeightMap(size: Size, heights: List<Int>): Array<Array<Tile>> {
    val rows = mutableListOf<List<Tile>>()
    for (x in 0..<size.width) {
      val column = mutableListOf<Tile>()
      for (y in 0..<size.height) {
        column.add(Tile(Position(x, y), heights[(y * size.width) + x]))
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
    return currentMap.mapData[position.x][position.y]
  }

  fun getFuelCost(oldPosition: Position, newPosition: Position): Int {
    return STATIC_FUEL_COST + abs(getTileAtPosition(oldPosition).height - getTileAtPosition(newPosition).height)
  }

  /***
   * Get all tiles in range of manhattan distance around the position.
   *
   * @return A pair of all tiles and the cost for the taken scan.
   */
  fun getTilesInDistance(position: Position, distance: Int): Pair<List<Tile>, Int> {
    var usedFuel = 0.0
    val list = mutableListOf<Tile>()
    for (x in (position.x - distance)..position.x + distance) {
      for (y in (position.y - distance)..position.y + distance) {
        val checked = Position(x, y)
        if (isPositionValid(checked) && checked.getDistanceTo(position) <= distance) {
          list.add(currentMap.mapData[x][y])
        }
        usedFuel += TILE_SCAN_COST
      }
    }
    return Pair(list, ceil(usedFuel).toInt())
  }
}
