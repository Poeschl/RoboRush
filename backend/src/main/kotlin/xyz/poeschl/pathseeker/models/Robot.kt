package xyz.poeschl.pathseeker.models

import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.v3.oas.annotations.media.Schema
import net.karneim.pojobuilder.GeneratePojoBuilder
import xyz.poeschl.pathseeker.configuration.Builder
import xyz.poeschl.pathseeker.gamelogic.actions.*
import xyz.poeschl.pathseeker.security.repository.User

@GeneratePojoBuilder(withBuilderInterface = Builder::class)
data class ActiveRobot(
  val id: Long,
  @JsonIgnore val user: User,
  val color: Color,
  var fuel: Int,
  var position: Position,
  @Schema(anyOf = [MoveAction::class, ScanAction::class, WaitAction::class, RefuelAction::class]) var nextAction: RobotAction<*>? = null,
  var lastResult: Any? = null
) {

  val maxFuel = fuel
}

@GeneratePojoBuilder(withBuilderInterface = Builder::class)
data class PublicRobot(val id: Long, val color: Color, var position: Position)

enum class Direction {
  NORTH,
  SOUTH,
  WEST,
  EAST
}
