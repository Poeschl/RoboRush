package xyz.poeschl.roborush.models

import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.v3.oas.annotations.media.Schema
import net.karneim.pojobuilder.GeneratePojoBuilder
import xyz.poeschl.roborush.configuration.Builder
import xyz.poeschl.roborush.gamelogic.actions.*
import xyz.poeschl.roborush.security.repository.User

@GeneratePojoBuilder(withBuilderInterface = Builder::class)
data class ActiveRobot(
  val id: Long,
  @JsonIgnore val user: User,
  val color: Color,
  var fuel: Int,
  var position: Position,
  @Schema(
    anyOf = [
      MoveAction::class, ScanAction::class, WaitAction::class, RefuelAction::class, SolarChargeAction::class
    ]
  ) var nextAction: RobotAction<*>? = null,
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
