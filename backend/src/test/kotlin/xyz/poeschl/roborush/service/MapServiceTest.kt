package xyz.poeschl.roborush.service

import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
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

class MapServiceTest {

  private val mapRepository = mockk<MapRepository>()
  private val tileRepository = mockk<TileRepository>()

  private val mapService = MapService(mapRepository, tileRepository)

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
