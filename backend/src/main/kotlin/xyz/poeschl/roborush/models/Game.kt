package xyz.poeschl.roborush.models

import net.karneim.pojobuilder.GeneratePojoBuilder
import xyz.poeschl.roborush.configuration.Builder
import xyz.poeschl.roborush.gamelogic.GameState

@GeneratePojoBuilder(withBuilderInterface = Builder::class)
data class Game(
  val currentState: GameState,
  val currentTurn: Int,
  val targetPosition: Position?,
  val solarChargePossible: Boolean,
  val gameTimeoutsInMillis: GameTimeouts
)

data class GameTimeouts(val waitForPlayers: Long, val waitForAction: Long, val gameEnd: Long)
