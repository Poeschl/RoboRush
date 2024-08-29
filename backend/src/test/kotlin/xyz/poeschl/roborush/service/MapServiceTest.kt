package xyz.poeschl.roborush.service

import io.mockk.*
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
import xyz.poeschl.roborush.repositories.TileRepository
import xyz.poeschl.roborush.test.utils.builder.Builders.Companion.a
import xyz.poeschl.roborush.test.utils.builder.GameLogicBuilder.Companion.`$Map`
import xyz.poeschl.roborush.test.utils.builder.GameLogicBuilder.Companion.`$Position`
import xyz.poeschl.roborush.test.utils.builder.GameLogicBuilder.Companion.`$Tile`
import xyz.poeschl.roborush.test.utils.builder.MapBuilder.Companion.`$MapAttributeSaveDto`
import xyz.poeschl.roborush.test.utils.builder.NativeTypes.Companion.`$Double`
import xyz.poeschl.roborush.test.utils.builder.NativeTypes.Companion.`$String`

class MapServiceTest {

  private val mapRepository = mockk<MapRepository>()
  private val tileRepository = mockk<TileRepository>()

  private val mapService = MapService(mapRepository, tileRepository)

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
  fun newMapFromHeightMap_multipleTarget() {
    // WHEN
    val inputImageStream = this::class.java.getResourceAsStream("/maps/multiple-target.png")!!
    val name = a(`$String`("name"))

    // THEN
    val genResult = mapService.createNewMapFromHeightMap(name, inputImageStream)

    // VERIFY
    assertThat(genResult.errors).hasSize(1)
    assertThat(genResult.map.size).isEqualTo(Size(2, 2))
    assertThat(genResult.map.mapData).isEqualTo(
      listOf(
        Tile(null, Position(0, 0), 128, TileType.START_TILE),
        Tile(null, Position(1, 0), 128, TileType.DEFAULT_TILE),
        Tile(null, Position(0, 1), 128, TileType.TARGET_TILE),
        Tile(null, Position(1, 1), 128, TileType.DEFAULT_TILE)
      )
    )
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

  @Test
  fun setMapAttributes() {
    // WHEN
    val map = a(`$Map`())
    val attribute = a(`$MapAttributeSaveDto`().withSolarChargeRate(a(`$Double`(0.0, 1.0))))

    every { mapRepository.save(map) } returnsArgument 0

    // THEN
    val result = mapService.setMapAttributes(map, attribute)

    // VERIFY
    verify { mapRepository.save(map) }
    assertThat(result.mapName).isEqualTo(attribute.mapName)
    assertThat(result.maxRobotFuel).isEqualTo(attribute.maxRobotFuel)
    assertThat(result.solarChargeRate).isEqualTo(attribute.solarChargeRate)
  }

  @Test
  fun setMapAttributes_above_1() {
    // WHEN
    val map = a(`$Map`())
    val attribute = a(`$MapAttributeSaveDto`().withSolarChargeRate(a(`$Double`(2.0, 10.0))))

    every { mapRepository.save(map) } returnsArgument 0

    // THEN
    val result = mapService.setMapAttributes(map, attribute)

    // VERIFY
    verify { mapRepository.save(map) }
    assertThat(result.solarChargeRate).isEqualTo(1.0)
  }

  @Test
  fun setMapAttributes_below_0() {
    val map = a(`$Map`())
    val attribute = a(`$MapAttributeSaveDto`().withSolarChargeRate(a(`$Double`(-10.0, 0.0))))

    every { mapRepository.save(map) } returnsArgument 0

    // THEN
    val result = mapService.setMapAttributes(map, attribute)

    // VERIFY
    verify { mapRepository.save(map) }
    assertThat(result.solarChargeRate).isEqualTo(0.0)
  }

  @Test
  fun setMapTile_new_start() {
    // WHEN
    val map = a(`$Map`().withPossibleStartPositions(mutableListOf()))
    val tileBuilder = `$Tile`().withType(TileType.DEFAULT_TILE)
    val existingTile = tileBuilder.build()
    val tile = tileBuilder.but().withType(TileType.START_TILE).build()

    map.addTile(existingTile)

    every { tileRepository.save(any()) } returnsArgument 0
    every { mapRepository.save(map) } returnsArgument 0

    // THEN
    val result = mapService.setMapTile(map, tile)

    // VERIFY
    verify { mapRepository.save(map) }
    assertThat(result.possibleStartPositions).containsOnly(tile.position)

    val savedTile = slot<Tile>()
    verify { tileRepository.save(capture(savedTile)) }
    assertThat(savedTile.captured.position).isEqualTo(existingTile.position)
    assertThat(savedTile.captured.height).isEqualTo(existingTile.height)
    assertThat(savedTile.captured.id).isEqualTo(existingTile.id)
    assertThat(savedTile.captured.type).isEqualTo(tile.type)
  }

  @Test
  fun setMapTile_remove_start() {
    // WHEN
    val tileBuilder = `$Tile`().withType(TileType.START_TILE)
    val existingTile = tileBuilder.build()
    val tile = tileBuilder.but().withType(TileType.DEFAULT_TILE).build()
    val map = a(`$Map`().withPossibleStartPositions(mutableListOf(a(`$Position`()), existingTile.position)))

    map.addTile(a(`$Tile`().withType(TileType.START_TILE)))
    map.addTile(existingTile)

    every { tileRepository.save(any()) } returnsArgument 0
    every { mapRepository.save(map) } returnsArgument 0

    // THEN
    val result = mapService.setMapTile(map, tile)

    // VERIFY
    verify { mapRepository.save(map) }
    assertThat(result.possibleStartPositions).doesNotContain(tile.position)
    assertThat(result.possibleStartPositions).hasSize(1)

    val savedTile = slot<Tile>()
    verify { tileRepository.save(capture(savedTile)) }
    assertThat(savedTile.captured.position).isEqualTo(existingTile.position)
    assertThat(savedTile.captured.height).isEqualTo(existingTile.height)
    assertThat(savedTile.captured.id).isEqualTo(existingTile.id)
    assertThat(savedTile.captured.type).isEqualTo(tile.type)
  }

  @Test
  fun setMapTile_remove_last_start() {
    // WHEN
    val tileBuilder = `$Tile`().withType(TileType.START_TILE)
    val existingTile = tileBuilder.build()
    val tile = tileBuilder.but().withType(TileType.DEFAULT_TILE).build()
    val map = a(`$Map`().withPossibleStartPositions(mutableListOf(existingTile.position)))

    map.addTile(a(`$Tile`().withType(TileType.START_TILE)))
    map.addTile(existingTile)

    every { tileRepository.save(any()) } returnsArgument 0
    every { mapRepository.save(map) } returnsArgument 0

    // THEN
    val result = mapService.setMapTile(map, tile)

    // VERIFY
    verify { mapRepository.save(map) }
    assertThat(result.possibleStartPositions).containsOnly(tile.position)
    assertThat(result.possibleStartPositions).hasSize(1)

    val savedTile = slot<Tile>()
    verify { tileRepository.save(capture(savedTile)) }
    assertThat(savedTile.captured.position).isEqualTo(existingTile.position)
    assertThat(savedTile.captured.height).isEqualTo(existingTile.height)
    assertThat(savedTile.captured.id).isEqualTo(existingTile.id)
    assertThat(savedTile.captured.type).isEqualTo(existingTile.type)
  }

  @Test
  fun setMapTile_add_target() {
    // WHEN
    val tileBuilder = `$Tile`().withType(TileType.DEFAULT_TILE)
    val existingTile = tileBuilder.build()
    val tile = tileBuilder.but().withType(TileType.TARGET_TILE).build()
    val map = a(`$Map`().withTargetPosition(a(`$Position`())))

    map.addTile(a(`$Tile`().withPosition(map.targetPosition).withType(TileType.TARGET_TILE)))
    map.addTile(existingTile)

    every { tileRepository.save(any()) } returnsArgument 0
    every { mapRepository.save(map) } returnsArgument 0

    // THEN
    val result = mapService.setMapTile(map, tile)

    // VERIFY
    verify { mapRepository.save(map) }
    assertThat(result.targetPosition).isEqualTo(tile.position)

    val savedTiles = mutableListOf<Tile>()
    verify { tileRepository.save(capture(savedTiles)) }
    assertThat(savedTiles).hasSize(2)

    val oldTileCapture = savedTiles[0]
    assertThat(oldTileCapture.type).isEqualTo(TileType.DEFAULT_TILE)

    val newTileCapture = savedTiles[1]
    assertThat(newTileCapture.position).isEqualTo(existingTile.position)
    assertThat(newTileCapture.height).isEqualTo(existingTile.height)
    assertThat(newTileCapture.id).isEqualTo(existingTile.id)
    assertThat(newTileCapture.type).isEqualTo(TileType.TARGET_TILE)
  }

  @Test
  fun setMapTile_remove_target() {
    // WHEN
    val tileBuilder = `$Tile`().withType(TileType.TARGET_TILE)
    val existingTile = tileBuilder.build()
    val tile = tileBuilder.but().withType(TileType.DEFAULT_TILE).build()
    val oldTargetPos = a(`$Position`())
    val map = a(`$Map`().withTargetPosition(oldTargetPos))

    map.addTile(existingTile)

    every { tileRepository.save(any()) } returnsArgument 0
    every { mapRepository.save(map) } returnsArgument 0

    // THEN
    val result = mapService.setMapTile(map, tile)

    // VERIFY
    verify { mapRepository.save(map) }
    assertThat(result.targetPosition).isEqualTo(oldTargetPos)

    val savedTile = slot<Tile>()
    verify { tileRepository.save(capture(savedTile)) }

    assertThat(savedTile.captured.position).isEqualTo(existingTile.position)
    assertThat(savedTile.captured.height).isEqualTo(existingTile.height)
    assertThat(savedTile.captured.id).isEqualTo(existingTile.id)
    assertThat(savedTile.captured.type).isEqualTo(existingTile.type)
  }

  @Test
  fun setMapTile_add_fuel_station() {
    // WHEN
    val tileBuilder = `$Tile`().withType(TileType.DEFAULT_TILE)
    val existingTile = tileBuilder.build()
    val tile = tileBuilder.but().withType(TileType.FUEL_TILE).build()
    val map = a(`$Map`().withTargetPosition(a(`$Position`())))

    map.addTile(existingTile)

    every { tileRepository.save(any()) } returnsArgument 0
    every { mapRepository.save(map) } returnsArgument 0

    // THEN
    val result = mapService.setMapTile(map, tile)

    // VERIFY
    verify { mapRepository.save(map) }

    val savedTile = slot<Tile>()
    verify { tileRepository.save(capture(savedTile)) }
    assertThat(savedTile.captured.position).isEqualTo(existingTile.position)
    assertThat(savedTile.captured.height).isEqualTo(existingTile.height)
    assertThat(savedTile.captured.id).isEqualTo(existingTile.id)
    assertThat(savedTile.captured.type).isEqualTo(tile.type)
  }

  @Test
  fun setMapTile_remove_fuel_station() {
    // WHEN
    val tileBuilder = `$Tile`().withType(TileType.FUEL_TILE)
    val existingTile = tileBuilder.build()
    val tile = tileBuilder.but().withType(TileType.DEFAULT_TILE).build()
    val map = a(`$Map`().withTargetPosition(a(`$Position`())))

    map.addTile(existingTile)

    every { tileRepository.save(any()) } returnsArgument 0
    every { mapRepository.save(map) } returnsArgument 0

    // THEN
    val result = mapService.setMapTile(map, tile)

    // VERIFY
    verify { mapRepository.save(map) }

    val savedTile = slot<Tile>()
    verify { tileRepository.save(capture(savedTile)) }
    assertThat(savedTile.captured.position).isEqualTo(existingTile.position)
    assertThat(savedTile.captured.height).isEqualTo(existingTile.height)
    assertThat(savedTile.captured.id).isEqualTo(existingTile.id)
    assertThat(savedTile.captured.type).isEqualTo(tile.type)
  }
}
