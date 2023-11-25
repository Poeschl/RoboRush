package xyz.poeschl.pathseeker.models

data class Robot(var id: Int, var fuel: Int, var position: Position)

enum class Direction {
  NORTH,
  SOUTH,
  WEST,
  EAST
}
