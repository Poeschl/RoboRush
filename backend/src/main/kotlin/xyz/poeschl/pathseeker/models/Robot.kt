package xyz.poeschl.pathseeker.models

import xyz.poeschl.pathseeker.gamelogic.actions.RobotAction

data class ActiveRobot(val id: Long, val color: Color, var fuel: Int, var position: Position, var nextAction: RobotAction? = null)

data class PublicRobot(val id: Long, val color: Color, var position: Position)

enum class Direction {
  NORTH,
  SOUTH,
  WEST,
  EAST
}
