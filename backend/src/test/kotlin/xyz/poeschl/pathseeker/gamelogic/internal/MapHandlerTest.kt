package xyz.poeschl.pathseeker.gamelogic.internal

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import xyz.poeschl.pathseeker.models.Position
import xyz.poeschl.pathseeker.models.Size
import xyz.poeschl.pathseeker.models.TileType
import xyz.poeschl.pathseeker.repositories.Map
import xyz.poeschl.pathseeker.repositories.Tile
import java.util.stream.IntStream
import java.util.stream.Stream
import kotlin.math.ceil
import kotlin.random.Random
import kotlin.streams.toList

class MapHandlerTest {

  companion object {
    @JvmStatic
    fun positionSource(): Stream<Arguments> = Stream.of(
      Arguments.of(Position(0, 0), true),
      Arguments.of(Position(1, 1), true),
      Arguments.of(Position(-1, 0), false),
      Arguments.of(Position(0, -1), false),
      Arguments.of(Position(2, 0), false),
      Arguments.of(Position(0, 2), false),
      Arguments.of(Position(-1, -1), false),
      Arguments.of(Position(2, 2), false)
    )
  }

  private val mapHandler = MapHandler()

  @ParameterizedTest
  @MethodSource("positionSource")
  fun isPositionValid(position: Position, isValid: Boolean) {
    // WHEN
    mapHandler.loadNewMap(createNewRandomMap(Size(2, 2)))

    // THEN
    val valid = mapHandler.isPositionValid(position)

    // VERIFY
    assertThat(valid).isEqualTo(isValid)
  }

  @Test
  fun getTileAtPosition() {
    // WHEN
    // Map:
    // 1 2
    // 3 4
    val map = createNewPresetMap(Size(2, 2), listOf(1, 2, 3, 4), Position(0, 0), Position(1, 1))
    mapHandler.loadNewMap(map)

    // THEN
    val tile1 = mapHandler.getTileAtPosition(Position(0, 0))
    val tile2 = mapHandler.getTileAtPosition(Position(1, 0))
    val tile3 = mapHandler.getTileAtPosition(Position(0, 1))
    val tile4 = mapHandler.getTileAtPosition(Position(1, 1))

    // VERIFY
    assertThat(tile1).isEqualTo(Tile(null, Position(0, 0), 1, TileType.START_TILE))
    assertThat(tile2).isEqualTo(Tile(null, Position(1, 0), 2))
    assertThat(tile3).isEqualTo(Tile(null, Position(0, 1), 3))
    assertThat(tile4).isEqualTo(Tile(null, Position(1, 1), 4, TileType.TARGET_TILE))
  }

  @Test
  fun getFuelCost() {
    // WHEN
    // Map:
    // 1 2
    // 3 4
    val map = createNewPresetMap(Size(2, 2), listOf(1, 2, 3, 4), Position(0, 0))
    mapHandler.loadNewMap(map)

    // THEN
    val fuel = mapHandler.getFuelCost(Position(0, 0), Position(0, 1))

    // VERIFY
    assertThat(fuel).isEqualTo(3) // max((3-1),0) + 1
  }

  @Test
  fun getFuelCost_freeRolling() {
    // WHEN
    // Map:
    // 1 2
    // 3 4
    val map = createNewPresetMap(Size(2, 2), listOf(1, 2, 3, 4), Position(0, 0))
    mapHandler.loadNewMap(map)

    // THEN
    val fuel = mapHandler.getFuelCost(Position(1, 1), Position(1, 0))

    // VERIFY
    assertThat(fuel).isEqualTo(1) // max((2-4), 0) + 1
  }

  @Test
  fun getTilesInDistance() {
    // WHEN
    // 1  2  3  4
    // 5  6  7  8
    // 9 10 11 12
    val map = createNewPresetMap(Size(4, 3), listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12), Position(0, 0), Position(3, 2))
    mapHandler.loadNewMap(map)

    // THEN
    val scans = mapHandler.getTilesInDistance(Position(2, 1), 1)

    // VERIFY
    assertThat(scans.first).contains(
      Tile(null, Position(2, 0), 3),
      Tile(null, Position(1, 1), 6),
      Tile(null, Position(2, 1), 7),
      Tile(null, Position(3, 1), 8),
      Tile(null, Position(2, 2), 11)
    )
    // Calculation is roundUp ((3 * 3) * 0,15)
    assertThat(scans.second).isEqualTo(ceil((3 * 3) * 0.15).toInt())
  }

  @Test
  fun getTilesInDistance_bigger() {
    // WHEN
    //  1  2  3  4  5  6  7
    //  8  9 10 11 12 13 14
    // 15 16 17 18 19 20 21
    // 22 23 24 25 26 27 28
    // 29 30 31 32 33 34 35
    // 36 37 38 39 40 41 42
    // 43 44 45 46 47 48 49
    val map = createNewRandomMap(Size(7, 7))
    mapHandler.loadNewMap(map)

    // THEN
    val scans = mapHandler.getTilesInDistance(Position(2, 2), 2)

    // VERIFY
    // Calculation is roundUp ((5 * 5) * 0,15)
    assertThat(scans.second).isEqualTo(ceil((5 * 5) * 0.15).toInt())
  }

  @Test
  fun loadNewMap() {
    // WHEN
    // 1  2  3  4
    // 5  6  7  8
    // 9 10 11 12
    val map = createNewPresetMap(Size(4, 3), listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12), Position(0, 0), Position(3, 2))

    // THEN
    mapHandler.loadNewMap(map)

    // VERIFY
    assertThat(mapHandler.getStartPositions()).containsOnly(Position(0, 0))
    assertThat(mapHandler.getTargetPosition()).isEqualTo(Position(3, 2))

    assertThat(mapHandler.getTileAtPosition(Position(0, 0))).isEqualTo(Tile(null, Position(0, 0), 1, TileType.START_TILE))
    assertThat(mapHandler.getTileAtPosition(Position(1, 1))).isEqualTo(Tile(null, Position(1, 1), 6))
    assertThat(mapHandler.getTileAtPosition(Position(3, 2))).isEqualTo(Tile(null, Position(3, 2), 12, TileType.TARGET_TILE))
  }

  private fun createNewRandomMap(size: Size): Map {
    val randomHeights = IntStream.range(0, size.width * size.height)
      .map { Random.nextInt(0, 8) }.toList()
    val startPositions = listOf(Position(0, 0), Position(0, 1), Position(1, 0), Position(1, 1))
    val targetPosition = Position(size.width - 2, size.height - 2)
    val map = Map(null, "gen", size, startPositions, targetPosition, 100)
    createHeightMap(size, randomHeights, startPositions, targetPosition).forEach { map.addTile(it) }
    return map
  }

  private fun createNewPresetMap(size: Size, heights: List<Int>, start: Position, target: Position = Position(size.width - 1, size.height - 1)): Map {
    val map = Map(null, "gen", size, listOf(start), target, 100)
    createHeightMap(size, heights, listOf(start), target).forEach { map.addTile(it) }
    return map
  }

  /**
   * Creates a new map with the heights given as a list.
   *
   * @param size The map size
   * @param heights The heights of the map as list. It continues horizontally.
   */
  private fun createHeightMap(size: Size, heights: List<Int>, startPositions: List<Position>, target: Position): List<Tile> {
    val tiles = mutableListOf<Tile>()
    for (y in 0..<size.height) {
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

        tiles.add(Tile(null, pos, heights[(y * size.width) + x], type))
      }
    }

    return tiles
  }
}
