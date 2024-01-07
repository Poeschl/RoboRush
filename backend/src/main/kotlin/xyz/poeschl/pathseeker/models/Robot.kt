package xyz.poeschl.pathseeker.models

import net.karneim.pojobuilder.GeneratePojoBuilder
import xyz.poeschl.pathseeker.configuration.Builder
import xyz.poeschl.pathseeker.gamelogic.actions.RobotAction

@GeneratePojoBuilder(withBuilderInterface = Builder::class)
data class ActiveRobot(
  val id: Long,
  val color: Color,
  var fuel: Int,
  var position: Position,
  var nextAction: RobotAction<*>? = null,
  var lastResult: Any? = null
)

@GeneratePojoBuilder(withBuilderInterface = Builder::class)
data class PublicRobot(val id: Long, val color: Color, var position: Position)

enum class Direction {
  NORTH,
  SOUTH,
  WEST,
  EAST
}
