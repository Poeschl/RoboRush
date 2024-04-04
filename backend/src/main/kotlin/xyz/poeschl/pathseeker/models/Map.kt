package xyz.poeschl.pathseeker.models

import net.karneim.pojobuilder.GeneratePojoBuilder
import xyz.poeschl.pathseeker.configuration.Builder
import kotlin.math.abs

@GeneratePojoBuilder(withBuilderInterface = Builder::class)
data class Map(val size: Size, val mapData: Array<Array<Tile>>, val possibleStartPositions: List<Position>, val targetPosition: Position) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Map

    if (size != other.size) return false
    if (!mapData.contentDeepEquals(other.mapData)) return false
    if (possibleStartPositions != other.possibleStartPositions) return false
    if (targetPosition != other.targetPosition) return false

    return true
  }

  override fun hashCode(): Int {
    var result = size.hashCode()
    result = 31 * result + mapData.contentDeepHashCode()
    result = 31 * result + possibleStartPositions.hashCode()
    result = 31 * result + targetPosition.hashCode()
    return result
  }
}

@GeneratePojoBuilder(withBuilderInterface = Builder::class)
data class Tile(val position: Position, val height: Int = 0, val type: TileType = TileType.DEFAULT_TILE)

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

enum class TileType {
  DEFAULT_TILE,
  START_TILE,
  TARGET_TILE
}

@GeneratePojoBuilder(withBuilderInterface = Builder::class)
data class Size(val width: Int, val height: Int)

data class InternalMapGenResult(val map: Map, val errors: List<String>)
