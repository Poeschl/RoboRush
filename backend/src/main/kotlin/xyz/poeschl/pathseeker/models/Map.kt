package xyz.poeschl.pathseeker.models

import kotlin.math.abs

data class Map(val size: Size, val heightMap: Array<Array<Tile>>) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Map

    if (size != other.size) return false
    if (!heightMap.contentDeepEquals(other.heightMap)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = size.hashCode()
    result = 31 * result + heightMap.contentDeepHashCode()
    return result
  }
}

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

data class Size(val width: Int, val height: Int)
