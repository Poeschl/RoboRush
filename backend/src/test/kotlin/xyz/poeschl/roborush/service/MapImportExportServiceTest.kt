package xyz.poeschl.roborush.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import xyz.poeschl.roborush.exceptions.NoStartingPosition
import xyz.poeschl.roborush.exceptions.NoTargetPosition
import xyz.poeschl.roborush.models.Color
import xyz.poeschl.roborush.models.Position
import xyz.poeschl.roborush.models.Size
import xyz.poeschl.roborush.models.TileType
import xyz.poeschl.roborush.repositories.Tile
import xyz.poeschl.roborush.test.utils.builder.Builders.Companion.a
import xyz.poeschl.roborush.test.utils.builder.GameLogicBuilder.Companion.`$Map`
import xyz.poeschl.roborush.test.utils.builder.GameLogicBuilder.Companion.`$Position`
import xyz.poeschl.roborush.test.utils.builder.GameLogicBuilder.Companion.`$Size`
import xyz.poeschl.roborush.test.utils.builder.GameLogicBuilder.Companion.`$Tile`
import xyz.poeschl.roborush.test.utils.builder.NativeTypes.Companion.`$String`
import java.io.ByteArrayInputStream
import java.util.stream.Stream
import javax.imageio.ImageIO

class MapImportExportServiceTest {

  companion object {
    @JvmStatic
    private fun tileTypeTestData(): Stream<Arguments> = Stream.of(
      Arguments.of(TileType.START_TILE, 1, Color(1, 255, 1)),
      Arguments.of(TileType.TARGET_TILE, 1, Color(255, 1, 1)),
      Arguments.of(TileType.FUEL_TILE, 1, Color(1, 1, 255)),
      Arguments.of(TileType.DEFAULT_TILE, 100, Color(100, 100, 100))
    )
  }

  private val importExportService = MapImportExportService()

  @Test
  fun exportMap() {
    // WHEN
    val map = a(`$Map`().withSize(a(`$Size`().withHeight(2).withWidth(2))))
    map.addTile(a(`$Tile`().withPosition(a(`$Position`().withX(0).withY(0))).withHeight(1).withType(TileType.DEFAULT_TILE)))
    map.addTile(a(`$Tile`().withPosition(a(`$Position`().withX(0).withY(1))).withHeight(2).withType(TileType.DEFAULT_TILE)))
    map.addTile(a(`$Tile`().withPosition(a(`$Position`().withX(1).withY(0))).withHeight(3).withType(TileType.DEFAULT_TILE)))
    map.addTile(a(`$Tile`().withPosition(a(`$Position`().withX(1).withY(1))).withHeight(4).withType(TileType.DEFAULT_TILE)))

    // THEN
    val byteResult = importExportService.exportMap(map)

    // VERIFY
    val resultImage = ImageIO.read(ByteArrayInputStream(byteResult))
    assertThat(resultImage.width).isEqualTo(map.size.width)
    assertThat(resultImage.height).isEqualTo(map.size.height)
    assertThat(Color.fromColorInt(resultImage.getRGB(0, 0))).isEqualTo(Color(1, 1, 1))
    assertThat(Color.fromColorInt(resultImage.getRGB(0, 1))).isEqualTo(Color(2, 2, 2))
    assertThat(Color.fromColorInt(resultImage.getRGB(1, 0))).isEqualTo(Color(3, 3, 3))
    assertThat(Color.fromColorInt(resultImage.getRGB(1, 1))).isEqualTo(Color(4, 4, 4))
  }

  @ParameterizedTest
  @MethodSource("tileTypeTestData")
  fun exportMap_TileTypes(tileType: TileType, height: Int, expectedColor: Color) {
    // WHEN
    val map = a(`$Map`().withSize(a(`$Size`().withHeight(1).withWidth(1))))
    map.addTile(a(`$Tile`().withPosition(a(`$Position`().withX(0).withY(0))).withHeight(height).withType(tileType)))

    // THEN
    val byteResult = importExportService.exportMap(map)

    // VERIFY
    val resultImage = ImageIO.read(ByteArrayInputStream(byteResult))
    assertThat(Color.fromColorInt(resultImage.getRGB(0, 0))).isEqualTo(expectedColor)
  }

  @Test
  fun importMap_correct() {
    // WHEN
    val inputImageStream = this::class.java.getResourceAsStream("/maps/correct.png")!!
    val name = a(`$String`("name"))

    // THEN
    val genResult = importExportService.importMap(name, inputImageStream)

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
  fun importMap_invalidGrey() {
    // WHEN
    val inputImageStream = this::class.java.getResourceAsStream("/maps/invalid-grey.png")!!
    val name = a(`$String`("name"))

    // THEN
    val genResult = importExportService.importMap(name, inputImageStream)

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
  fun importMap_noStart() {
    // WHEN
    val inputImageStream = this::class.java.getResourceAsStream("/maps/no-spawn.png")!!
    val name = a(`$String`("name"))

    // THEN + VERIFY
    assertThatThrownBy {
      importExportService.importMap(name, inputImageStream)
    }
      .isInstanceOf(NoStartingPosition::class.java)
      .hasMessageContaining("one starting position is required")
  }

  @Test
  fun importMap_noTarget() {
    // WHEN
    val inputImageStream = this::class.java.getResourceAsStream("/maps/no-target.png")!!
    val name = a(`$String`("name"))

    // THEN + VERIFY
    assertThatThrownBy {
      importExportService.importMap(name, inputImageStream)
    }
      .isInstanceOf(NoTargetPosition::class.java)
      .hasMessageContaining("one target position is required")
  }

  @Test
  fun importMap_multipleTarget() {
    // WHEN
    val inputImageStream = this::class.java.getResourceAsStream("/maps/multiple-target.png")!!
    val name = a(`$String`("name"))

    // THEN
    val genResult = importExportService.importMap(name, inputImageStream)

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
  fun importMap_fuelTile() {
    // WHEN
    val inputImageStream = this::class.java.getResourceAsStream("/maps/with-fuel.png")!!
    val name = a(`$String`("name"))

    // THEN
    val genResult = importExportService.importMap(name, inputImageStream)

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
