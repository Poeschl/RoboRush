package xyz.poeschl.roborush.models

import net.karneim.pojobuilder.GeneratePojoBuilder
import xyz.poeschl.roborush.configuration.Builder
import xyz.poeschl.roborush.gamelogic.GameState
import kotlin.time.Duration

@GeneratePojoBuilder(withBuilderInterface = Builder::class)
data class Game(
  val currentState: GameState,
  val currentTurn: Int,
  val targetPosition: Position?,
  val solarChargePossible: Boolean,
  val gameTimeouts: GameTimeouts
)

data class GameTimeouts(val waitForPlayers: Duration, val waitForAction: Duration, val gameEnd: Duration)
