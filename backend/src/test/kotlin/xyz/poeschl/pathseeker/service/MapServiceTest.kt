package xyz.poeschl.pathseeker.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import xyz.poeschl.pathseeker.models.Position
import xyz.poeschl.pathseeker.models.Size
import xyz.poeschl.pathseeker.models.Tile
import java.util.stream.Stream
import kotlin.math.ceil

class MapServiceTest {

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

  private val mapService = MapService()

  @ParameterizedTest
  @MethodSource("positionSource")
  fun isPositionValid(position: Position, isValid: Boolean) {
    // WHEN
    mapService.createNewRandomMap(Size(2, 2))

    // THEN
    val valid = mapService.isPositionValid(position)

    // VERIFY
    assertThat(valid).isEqualTo(isValid)
  }

  @Test
  fun getTileAtPosition() {
    // WHEN
    // Map:
    // 1 2
    // 3 4
    mapService.createNewPresetMap(Size(2, 2), listOf(1, 2, 3, 4))

    // THEN
    val tile1 = mapService.getTileAtPosition(Position(0, 0))
    val tile2 = mapService.getTileAtPosition(Position(1, 0))
    val tile3 = mapService.getTileAtPosition(Position(0, 1))
    val tile4 = mapService.getTileAtPosition(Position(1, 1))

    // VERIFY
    assertThat(tile1).isEqualTo(Tile(Position(0, 0), 1))
    assertThat(tile2).isEqualTo(Tile(Position(1, 0), 2))
    assertThat(tile3).isEqualTo(Tile(Position(0, 1), 3))
    assertThat(tile4).isEqualTo(Tile(Position(1, 1), 4))
  }

  @Test
  fun getFuelCost() {
    // WHEN
    // Map:
    // 1 2
    // 3 4
    mapService.createNewPresetMap(Size(2, 2), listOf(1, 2, 3, 4))

    // THEN
    val fuel = mapService.getFuelCost(Position(0, 0), Position(0, 1))

    // VERIFY
    assertThat(fuel).isEqualTo(3) // (3-1) + 1
  }

  @Test
  fun getTilesInDistance() {
    // WHEN
    // 1 2 3
    // 4 5 6
    // 7 8 9
    mapService.createNewPresetMap(Size(3, 3), listOf(1, 2, 3, 4, 5, 6, 7, 8, 9))

    // THEN
    val scans = mapService.getTilesInDistance(Position(1, 1), 1)

    // VERIFY
    assertThat(scans.first).contains(
      Tile(Position(1, 0), 2),
      Tile(Position(0, 1), 4),
      Tile(Position(1, 1), 5),
      Tile(Position(2, 1), 6),
      Tile(Position(1, 2), 8)
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
    mapService.createNewRandomMap(Size(7, 7))

    // THEN
    val scans = mapService.getTilesInDistance(Position(2, 2), 2)

    // VERIFY
    // Calculation is roundUp ((5 * 5) * 0,15)
    assertThat(scans.second).isEqualTo(ceil((5 * 5) * 0.15).toInt())
  }
}
