package xyz.poeschl.pathseeker.gamelogic.internal

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import xyz.poeschl.pathseeker.models.Position
import xyz.poeschl.pathseeker.models.Size
import xyz.poeschl.pathseeker.models.Tile
import xyz.poeschl.pathseeker.models.TileType
import java.util.stream.Stream
import kotlin.math.ceil

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
    mapHandler.createNewRandomMap(Size(2, 2))

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
    mapHandler.createNewPresetMap(Size(2, 2), listOf(1, 2, 3, 4), Position(0, 0), Position(1, 1))

    // THEN
    val tile1 = mapHandler.getTileAtPosition(Position(0, 0))
    val tile2 = mapHandler.getTileAtPosition(Position(1, 0))
    val tile3 = mapHandler.getTileAtPosition(Position(0, 1))
    val tile4 = mapHandler.getTileAtPosition(Position(1, 1))

    // VERIFY
    assertThat(tile1).isEqualTo(Tile(Position(0, 0), 1, TileType.START_TILE))
    assertThat(tile2).isEqualTo(Tile(Position(1, 0), 2))
    assertThat(tile3).isEqualTo(Tile(Position(0, 1), 3))
    assertThat(tile4).isEqualTo(Tile(Position(1, 1), 4, TileType.TARGET_TILE))
  }

  @Test
  fun getFuelCost() {
    // WHEN
    // Map:
    // 1 2
    // 3 4
    mapHandler.createNewPresetMap(Size(2, 2), listOf(1, 2, 3, 4), Position(0, 0))

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
    mapHandler.createNewPresetMap(Size(2, 2), listOf(1, 2, 3, 4), Position(0, 0))

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
    mapHandler.createNewPresetMap(Size(4, 3), listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12), Position(0, 0))

    // THEN
    val scans = mapHandler.getTilesInDistance(Position(2, 1), 1)

    // VERIFY
    assertThat(scans.first).contains(
      Tile(Position(2, 0), 3),
      Tile(Position(1, 1), 6),
      Tile(Position(2, 1), 7),
      Tile(Position(3, 1), 8),
      Tile(Position(2, 2), 11)
    )
    // Calculation is roundUp ((3 * 3) * 0,15)
    assertThat(scans.second).isEqualTo(ceil((3 * 3) * 0.15).toInt())
  }

  @Test
  fun getTilesInDistance_bigger() {
    // WHEN
    // 1 2 3
    // 4 5 6
    // 7 8 9
    mapHandler.createNewRandomMap(Size(7, 7))

    // THEN
    val scans = mapHandler.getTilesInDistance(Position(2, 2), 2)

    // VERIFY
    // Calculation is roundUp ((5 * 5) * 0,15)
    assertThat(scans.second).isEqualTo(ceil((5 * 5) * 0.15).toInt())
  }
}
