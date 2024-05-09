package xyz.poeschl.roborush.service

import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import xyz.poeschl.roborush.exceptions.NoStartingPosition
import xyz.poeschl.roborush.exceptions.NoTargetPosition
import xyz.poeschl.roborush.models.Position
import xyz.poeschl.roborush.models.Size
import xyz.poeschl.roborush.models.TileType
import xyz.poeschl.roborush.repositories.MapRepository
import xyz.poeschl.roborush.repositories.Tile
import xyz.poeschl.roborush.test.utils.builder.Builders.Companion.a
import xyz.poeschl.roborush.test.utils.builder.NativeTypes.Companion.`$String`

class MapServiceTest {

  private val mapRepository = mockk<MapRepository>()

  private val mapService = MapService(mapRepository)

  @Test
  fun newMapFromHeightMap_correct() {
    // WHEN
    val inputImageStream = this::class.java.getResourceAsStream("/maps/correct.png")!!
    val name = a(`$String`("name"))

    // THEN
    val genResult = mapService.createNewMapFromHeightMap(name, inputImageStream)

    // VERIFY
    assertThat(genResult.errors).isEmpty()
    assertThat(genResult.map.mapName).isEqualTo(name)
    assertThat(genResult.map.size).isEqualTo(Size(2, 2))
    assertThat(genResult.map.targetPosition).isEqualTo(Position(1, 1))
    assertThat(genResult.map.possibleStartPositions).containsOnly(Position(0, 0))
    assertThat(genResult.map.mapData).isEqualTo(
      listOf(
        Tile(null, Position(0, 0), 128, TileType.START_TILE),
        Tile(null, Position(1, 0), 128, TileType.DEFAULT_TILE),
        Tile(null, Position(0, 1), 200, TileType.DEFAULT_TILE),
        Tile(null, Position(1, 1), 128, TileType.TARGET_TILE)
      )
    )
  }

  @Test
  fun newMapFromHeightMap_invalidGrey() {
    // WHEN
    val inputImageStream = this::class.java.getResourceAsStream("/maps/invalid-grey.png")!!
    val name = a(`$String`("name"))

    // THEN
    val genResult = mapService.createNewMapFromHeightMap(name, inputImageStream)

    // VERIFY
    assertThat(genResult.errors[0]).contains("(1,0) with color (255, 128, 0)")
    assertThat(genResult.errors[1]).contains("(0,1) with color (255, 128, 0)")
    assertThat(genResult.map.size).isEqualTo(Size(2, 2))
    assertThat(genResult.map.targetPosition).isEqualTo(Position(1, 1))
    assertThat(genResult.map.possibleStartPositions).containsOnly(Position(0, 0))
    assertThat(genResult.map.mapData).isEqualTo(
      listOf(
        Tile(null, Position(0, 0), 128, TileType.START_TILE),
        Tile(null, Position(1, 0), 0, TileType.DEFAULT_TILE),
        Tile(null, Position(0, 1), 0, TileType.DEFAULT_TILE),
        Tile(null, Position(1, 1), 128, TileType.TARGET_TILE)
      )
    )
  }

  @Test
  fun newMapFromHeightMap_noStart() {
    // WHEN
    val inputImageStream = this::class.java.getResourceAsStream("/maps/no-spawn.png")!!
    val name = a(`$String`("name"))

    // THEN + VERIFY
    assertThatThrownBy {
      mapService.createNewMapFromHeightMap(name, inputImageStream)
    }
      .isInstanceOf(NoStartingPosition::class.java)
      .hasMessageContaining("one starting position is required")
  }

  @Test
  fun newMapFromHeightMap_noTarget() {
    // WHEN
    val inputImageStream = this::class.java.getResourceAsStream("/maps/no-target.png")!!
    val name = a(`$String`("name"))

    // THEN + VERIFY
    assertThatThrownBy {
      mapService.createNewMapFromHeightMap(name, inputImageStream)
    }
      .isInstanceOf(NoTargetPosition::class.java)
      .hasMessageContaining("one target position is required")
  }

  @Test
  fun newMapFromHeightMap_fuelTile() {
    // WHEN
    val inputImageStream = this::class.java.getResourceAsStream("/maps/with-fuel.png")!!
    val name = a(`$String`("name"))

    // THEN
    val genResult = mapService.createNewMapFromHeightMap(name, inputImageStream)

    // VERIFY
    assertThat(genResult.errors).isEmpty()
    assertThat(genResult.map.size).isEqualTo(Size(2, 2))
    assertThat(genResult.map.mapData).isEqualTo(
      listOf(
        Tile(null, Position(0, 0), 128, TileType.START_TILE),
        Tile(null, Position(1, 0), 128, TileType.FUEL_TILE),
        Tile(null, Position(0, 1), 200, TileType.DEFAULT_TILE),
        Tile(null, Position(1, 1), 128, TileType.TARGET_TILE)
      )
    )
  }
}
