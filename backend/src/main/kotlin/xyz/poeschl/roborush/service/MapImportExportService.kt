package xyz.poeschl.roborush.service

import org.apache.commons.imaging.Imaging
import org.apache.commons.imaging.formats.png.PngImagingParameters
import org.apache.commons.imaging.formats.png.PngWriter
import org.apache.xmlgraphics.util.QName
import org.apache.xmlgraphics.xmp.Metadata
import org.apache.xmlgraphics.xmp.XMPParser
import org.apache.xmlgraphics.xmp.XMPProperty
import org.apache.xmlgraphics.xmp.XMPSerializer
import org.slf4j.LoggerFactory
import org.springframework.core.io.InputStreamSource
import org.springframework.stereotype.Service
import org.xml.sax.SAXException
import xyz.poeschl.roborush.exceptions.NoStartingPosition
import xyz.poeschl.roborush.exceptions.NoTargetPosition
import xyz.poeschl.roborush.exceptions.UnknownTileType
import xyz.poeschl.roborush.models.*
import xyz.poeschl.roborush.repositories.Map
import xyz.poeschl.roborush.repositories.Tile
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.*
import javax.imageio.ImageIO
import javax.xml.transform.TransformerConfigurationException
import javax.xml.transform.stream.StreamSource
import kotlin.jvm.optionals.getOrNull
import kotlin.time.measureTime

@Service
class MapImportExportService {

  companion object {
    private val LOGGER = LoggerFactory.getLogger(MapImportExportService::class.java)

    private const val XMP_URI = "https://github.com/Poeschl/RoboRush"
    private const val XMP_PREFIX = "rr:"
    private const val XMP_MAP_SOLAR_CHARGE_RATE_KEY = "${XMP_PREFIX}solarChargeRate"
    private const val XMP_MAP_MAX_ROBOT_FUEL_KEY = "${XMP_PREFIX}maxRobotFuel"
  }

  fun exportMap(map: Map): ByteArray {
    val image = BufferedImage(map.size.width, map.size.height, BufferedImage.TYPE_INT_RGB)
    val graphics = image.createGraphics()

    map.mapData.forEach { drawTile(graphics, it) }
    graphics.dispose()

    val imageBytesWithMetadata = setMapMetadata(image, MapMetadata(map.solarChargeRate, map.maxRobotFuel))
    return imageBytesWithMetadata
  }

  private fun drawTile(graphics: Graphics, tile: Tile) {
    graphics.color = getTileColor(tile).toAwtColor()
    graphics.drawRect(tile.position.x, tile.position.y, 1, 1)
  }

  private fun getTileColor(tile: Tile): Color = when (tile.type) {
    TileType.TARGET_TILE -> Color(255, tile.height, tile.height)
    TileType.START_TILE -> Color(tile.height, 255, tile.height)
    TileType.FUEL_TILE -> Color(tile.height, tile.height, 255)
    TileType.DEFAULT_TILE -> Color(tile.height, tile.height, tile.height)
  }

  /**
   * Generates a new map object from the given height map image and returns all detected recoverable errors as result.
   *
   * @return A list of detected errors as string list. All of those errors are somehow escaped, but the map might not be ideal.
   */
  fun importMap(mapName: String, heightMapFile: InputStreamSource): InternalMapGenResult {
    LOGGER.info("Create new map from height map image")

    val result: InternalMapGenResult
    val generationDuration = measureTime {
      result = convertHeightImageToMap(mapName, heightMapFile)
    }

    LOGGER.info("Created map '{}' ({}x{}) in {} ms", result.map.mapName, result.map.size.width, result.map.size.height, generationDuration.inWholeMilliseconds)
    return result
  }

  private fun convertHeightImageToMap(mapName: String, heightMapFile: InputStreamSource): InternalMapGenResult {
    val image = ImageIO.read(heightMapFile.inputStream.buffered())

    val startingPositions = mutableListOf<Position>()
    var targetPosition: Position? = null
    val tiles = mutableListOf<Tile>()
    val errors = mutableListOf<String>()

    for (y in 0..<image.height) {
      for (x in 0..<image.width) {
        val pos = Position(x, y)
        val pixelColor = Color.fromColorInt(image.getRGB(x, y))

        var tileData: TileData
        try {
          tileData = getTileData(pixelColor)
        } catch (ex: UnknownTileType) {
          LOGGER.warn("Unknown tile type detected at ({},{}) with color {}. Inserting default!", pos.x, pos.y, pixelColor.toString())
          errors.add("Unknown tile type detected at (%d,%d) with color (%d, %d, %d).".format(pos.x, pos.y, pixelColor.r, pixelColor.g, pixelColor.b))
          tileData = TileData(0, TileType.DEFAULT_TILE)
        }

        when (tileData.type) {
          TileType.DEFAULT_TILE -> tiles.add(Tile(null, pos, tileData.height, tileData.type))
          TileType.FUEL_TILE -> tiles.add(Tile(null, pos, tileData.height, tileData.type))

          TileType.START_TILE -> {
            tiles.add(Tile(null, pos, tileData.height, tileData.type))
            startingPositions.add(pos)
            LOGGER.debug("Detected start point at ({},{})", pos.x, pos.y)
          }

          TileType.TARGET_TILE -> {
            if (targetPosition == null) {
              tiles.add(Tile(null, pos, tileData.height, tileData.type))
              targetPosition = pos
              LOGGER.debug("Detected target point at ({},{})", pos.x, pos.y)
            } else {
              errors.add("Multiple target positions detected. Using first one (%d,%d) and skip all others!".format(pos.x, pos.y))
              tiles.add(Tile(null, pos, tileData.height, TileType.DEFAULT_TILE))
            }
          }
        }
      }
    }

    if (startingPositions.isEmpty()) {
      LOGGER.warn("No starting position detected")
      throw NoStartingPosition("At least one starting position is required")
    }

    if (targetPosition == null) {
      LOGGER.warn("No target position detected")
      throw NoTargetPosition("At least one target position is required")
    }

    val map = Map(null, mapName, Size(image.width, image.height), startingPositions, targetPosition)
    // Add all tiles to map for the db relations
    tiles.forEach { map.addTile(it) }

    val mapMetadata = getMapMetadata(heightMapFile.inputStream.buffered())
    mapMetadata?.let {
      // use existing metadata, if no metadata exists use defaults
      // same for single missing values
      mapMetadata.solarChargeRate?.let {
        map.solarChargeRate = mapMetadata.solarChargeRate
      }
      mapMetadata.maxRobotFuel?.let {
        map.maxRobotFuel = mapMetadata.maxRobotFuel
      }
    }

    return InternalMapGenResult(map, errors)
  }

  private data class TileData(val height: Int, val type: TileType)

  private fun getTileData(color: Color): TileData {
    return when {
      color.isGrey() -> {
        val height = color.r
        TileData(height, TileType.DEFAULT_TILE)
      }

      color.g > color.r && color.r == color.b -> {
        // starting points
        val height = color.r
        TileData(height, TileType.START_TILE)
      }

      color.r > color.g && color.g == color.b -> {
        // target points
        val height = color.g
        TileData(height, TileType.TARGET_TILE)
      }

      color.b > color.r && color.g == color.r -> {
        val height = color.r
        TileData(height, TileType.FUEL_TILE)
      }

      else -> {
        throw UnknownTileType("Unknown tile type detected")
      }
    }
  }

  private fun setMapMetadata(imageInput: BufferedImage, mapMetadata: MapMetadata): ByteArray {
    val xmpMetadata = Metadata()

    // Set metadata form stored map attributes
    val solarChargeProp = XMPProperty(QName(XMP_URI, XMP_MAP_SOLAR_CHARGE_RATE_KEY), mapMetadata.solarChargeRate!!)
    xmpMetadata.setProperty(solarChargeProp)
    val maxRobotFuelProp = XMPProperty(QName(XMP_URI, XMP_MAP_MAX_ROBOT_FUEL_KEY), mapMetadata.maxRobotFuel!!)
    xmpMetadata.setProperty(maxRobotFuelProp)

    try {
      val xmpString = ByteArrayOutputStream().use {
        XMPSerializer.writeXMPPacket(xmpMetadata, it, false)
        return@use it.toString(StandardCharsets.UTF_8)
      }

      return ByteArrayOutputStream().use {
        val pngParams = PngImagingParameters()
        pngParams.isForceTrueColor = true
        pngParams.xmpXml = xmpString
        PngWriter().writeImage(imageInput, it, pngParams, null)
        return@use it.toByteArray()
      }
    } catch (ex: TransformerConfigurationException) {
      LOGGER.warn("Couldn't write metadata!", ex)
    } catch (ex: SAXException) {
      LOGGER.warn("Couldn't write metadata!", ex)
    }

    // In case of error write image without metadata
    return ByteArrayOutputStream().use {
      val pngParams = PngImagingParameters()
      pngParams.isForceTrueColor = true
      PngWriter().writeImage(imageInput, it, pngParams, null)
      return@use it.toByteArray()
    }
  }

  private fun getMapMetadata(imageInput: InputStream): MapMetadata? {
    val xmpString = Imaging.getXmpXml(imageInput.readAllBytes())
    if (xmpString != null && xmpString.isNotEmpty() && xmpString.contains(XMP_URI)) {
      val xmpMetadata = XMPParser.parseXMP(StreamSource(xmpString.byteInputStream().buffered()))

      val solarChargeProp = Optional.ofNullable(xmpMetadata.getProperty(XMP_URI, XMP_MAP_SOLAR_CHARGE_RATE_KEY)).getOrNull()
      val maxRobotFuelProp = Optional.ofNullable(xmpMetadata.getProperty(XMP_URI, XMP_MAP_MAX_ROBOT_FUEL_KEY)).getOrNull()

      return MapMetadata(
        (solarChargeProp?.value as String).toDoubleOrNull(),
        (maxRobotFuelProp?.value as String).toIntOrNull()
      )
    } else {
      return null
    }
  }
}
