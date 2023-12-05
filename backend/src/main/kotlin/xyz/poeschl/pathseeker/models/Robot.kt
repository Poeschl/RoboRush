package xyz.poeschl.pathseeker.models

data class Robot(val id: Int, val color: Color, var fuel: Int, var position: Position)

data class PublicRobot(val id: Int, val color: Color, var position: Position)

enum class Direction {
  NORTH,
  SOUTH,
  WEST,
  EAST
}
