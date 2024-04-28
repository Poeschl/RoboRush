package xyz.poeschl.pathseeker.models

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import net.karneim.pojobuilder.GeneratePojoBuilder
import xyz.poeschl.pathseeker.configuration.Builder
import xyz.poeschl.pathseeker.repositories.Map
import kotlin.math.abs

@GeneratePojoBuilder(withBuilderInterface = Builder::class)
data class Position(val x: Int, val y: Int) {
  fun eastPosition(): Position {
    return Position(x + 1, y)
  }

  fun westPosition(): Position {
    return Position(x - 1, y)
  }

  fun northPosition(): Position {
    return Position(x, y - 1)
  }

  fun southPosition(): Position {
    return Position(x, y + 1)
  }

  fun getDistanceTo(other: Position): Int {
    return abs(this.x - other.x) + abs(this.y - other.y)
  }
}

@Converter(autoApply = true)
class PositionConverter : AttributeConverter<Position, String> {

  override fun convertToDatabaseColumn(attribute: Position): String {
    return "${attribute.x},${attribute.y}"
  }

  override fun convertToEntityAttribute(dbData: String): Position {
    val split = dbData.split(",").map { it.toInt() }
    return Position(split[0], split[1])
  }
}

@Converter(autoApply = true)
class PositionListConverter : AttributeConverter<List<Position>, String> {

  private val positionConverter = PositionConverter()

  override fun convertToDatabaseColumn(attribute: List<Position>): String {
    return attribute.map(positionConverter::convertToDatabaseColumn).joinToString("|")
  }

  override fun convertToEntityAttribute(dbData: String): List<Position> {
    return dbData.split("|").map(positionConverter::convertToEntityAttribute)
  }
}

enum class TileType {
  DEFAULT_TILE,
  START_TILE,
  TARGET_TILE,
  FUEL_TILE
}

@GeneratePojoBuilder(withBuilderInterface = Builder::class)
data class Size(val width: Int, val height: Int)

@Converter(autoApply = true)
class SizeConverter : AttributeConverter<Size, String> {
  override fun convertToDatabaseColumn(attribute: Size): String {
    return "${attribute.width}x${attribute.height}"
  }

  override fun convertToEntityAttribute(dbData: String): Size {
    val split = dbData.split("x").map { it.toInt() }
    return Size(split[0], split[1])
  }
}

data class InternalMapGenResult(val map: Map, val errors: List<String>)
