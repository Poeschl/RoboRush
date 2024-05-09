package xyz.poeschl.roborush.models

import net.karneim.pojobuilder.GeneratePojoBuilder
import xyz.poeschl.roborush.configuration.Builder
import xyz.poeschl.roborush.gamelogic.GameState

@GeneratePojoBuilder(withBuilderInterface = Builder::class)
data class Game(val currentState: GameState, val targetPosition: Position?, val solarChargePossible: Boolean)
