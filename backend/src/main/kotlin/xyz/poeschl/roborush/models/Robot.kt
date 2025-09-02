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
  val name: String,
  @JsonIgnore val user: User,
  val color: Color,
  var fuel: Int,
  var position: Position,
  @field:Schema(
    anyOf = [
      MoveAction::class, ScanAction::class, WaitAction::class, RefuelAction::class, SolarChargeAction::class
    ]
  ) var nextAction: RobotAction<*>? = null,
  @field:Schema(
    anyOf = [
      Int::class, ScanAction.ScanResult::class
    ]
  )
  var lastResult: Result? = null,
  @JsonIgnore
  val knownPositions: MutableSet<Position> = mutableSetOf()
) {

  val maxFuel = fuel

  fun canRetrieveFuel() = fuel < maxFuel

  fun addFuel(addedFuel: Int) {
    fuel = (fuel + addedFuel).coerceAtMost(maxFuel)
  }

  fun hasSufficientFuel(expectedFuel: Int) = expectedFuel <= fuel

  fun useFuel(usedFuel: Int) {
    fuel -= usedFuel
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as ActiveRobot

    return id == other.id
  }

  override fun hashCode(): Int = id.hashCode()
}

@GeneratePojoBuilder(withBuilderInterface = Builder::class)
data class PublicRobot(val id: Long, val name: String, val color: Color, var position: Position)

data class ScoreboardEntry(val name: String, val color: Color, val score: Int)

enum class Direction {
  NORTH,
  SOUTH,
  WEST,
  EAST
}
