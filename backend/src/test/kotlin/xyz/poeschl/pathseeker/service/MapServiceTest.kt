package xyz.poeschl.pathseeker.service

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import xyz.poeschl.pathseeker.exceptions.NoStartingPosition
import xyz.poeschl.pathseeker.exceptions.NoTargetPosition
import xyz.poeschl.pathseeker.gamelogic.GameHandler
import xyz.poeschl.pathseeker.models.Position
import xyz.poeschl.pathseeker.models.Size
import xyz.poeschl.pathseeker.models.Tile
import xyz.poeschl.pathseeker.models.TileType
import xyz.poeschl.pathseeker.test.utils.builder.Builders.Companion.listWithOne
import xyz.poeschl.pathseeker.test.utils.builder.GameLogicBuilder.Companion.`$Tile`

class MapServiceTest {

  private val gameHandler = mockk<GameHandler>()

  private val mapService = MapService(gameHandler)

  @Test
  fun getHeightMap() {
    // WHEN
    val tiles = listWithOne(`$Tile`())
    every { gameHandler.getHeightMap() } returns tiles

    // THEN
    val result = mapService.getHeightMap()

    // VERIFY
    assertThat(result).containsExactlyElementsOf(tiles)
  }

  @Test
  fun newMapFromHeightMap_correct() {
    // WHEN
    val inputImageStream = this::class.java.getResourceAsStream("/maps/correct.png")!!

    // THEN
    val genResult = mapService.createNewMapFromHeightMap(inputImageStream)

    // VERIFY
    assertThat(genResult.errors).isEmpty()
    assertThat(genResult.map.size).isEqualTo(Size(2, 2))
    assertThat(genResult.map.targetPosition).isEqualTo(Position(1, 1))
    assertThat(genResult.map.possibleStartPositions).containsOnly(Position(0, 0))
    assertThat(genResult.map.mapData).isEqualTo(
      arrayOf(
        Tile(Position(0, 0), 128, TileType.START_TILE),
        Tile(Position(1, 0), 128, TileType.DEFAULT_TILE),
        Tile(Position(0, 1), 200, TileType.DEFAULT_TILE),
        Tile(Position(1, 1), 128, TileType.TARGET_TILE)
      )
    )
  }

  @Test
  fun newMapFromHeightMap_invalidGrey() {
    // WHEN
    val inputImageStream = this::class.java.getResourceAsStream("/maps/invalid-grey.png")!!

    // THEN
    val genResult = mapService.createNewMapFromHeightMap(inputImageStream)

    // VERIFY
    assertThat(genResult.errors[0]).contains("(1,0) with color (255, 128, 0)")
    assertThat(genResult.errors[1]).contains("(0,1) with color (255, 128, 0)")
    assertThat(genResult.map.size).isEqualTo(Size(2, 2))
    assertThat(genResult.map.targetPosition).isEqualTo(Position(1, 1))
    assertThat(genResult.map.possibleStartPositions).containsOnly(Position(0, 0))
    assertThat(genResult.map.mapData).isEqualTo(
      arrayOf(
        Tile(Position(0, 0), 128, TileType.START_TILE),
        Tile(Position(1, 0), 0, TileType.DEFAULT_TILE),
        Tile(Position(0, 1), 0, TileType.DEFAULT_TILE),
        Tile(Position(1, 1), 128, TileType.TARGET_TILE)
      )
    )
  }

  @Test
  fun newMapFromHeightMap_noStart() {
    // WHEN
    val inputImageStream = this::class.java.getResourceAsStream("/maps/no-spawn.png")!!

    // THEN + VERIFY
    assertThatThrownBy {
      mapService.createNewMapFromHeightMap(inputImageStream)
    }
      .isInstanceOf(NoStartingPosition::class.java)
      .hasMessageContaining("one starting position is required")
  }

  @Test
  fun newMapFromHeightMap_noTarget() {
    // WHEN
    val inputImageStream = this::class.java.getResourceAsStream("/maps/no-target.png")!!

    // THEN + VERIFY
    assertThatThrownBy {
      mapService.createNewMapFromHeightMap(inputImageStream)
    }
      .isInstanceOf(NoTargetPosition::class.java)
      .hasMessageContaining("one target position is required")
  }
}
