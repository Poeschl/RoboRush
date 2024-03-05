package xyz.poeschl.pathseeker.models

import net.karneim.pojobuilder.GeneratePojoBuilder
import xyz.poeschl.pathseeker.configuration.Builder
import xyz.poeschl.pathseeker.gamelogic.GameState

@GeneratePojoBuilder(withBuilderInterface = Builder::class)
data class Game(val currentState: GameState)
