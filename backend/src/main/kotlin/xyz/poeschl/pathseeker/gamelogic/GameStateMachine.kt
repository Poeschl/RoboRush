package xyz.poeschl.pathseeker.gamelogic

import org.slf4j.LoggerFactory
import xyz.poeschl.pathseeker.configuration.GameLogic
import xyz.poeschl.pathseeker.exceptions.InvalidGameStateException

@GameLogic
class GameStateMachine {

  companion object {
    private val LOGGER = LoggerFactory.getLogger(GameStateMachine::class.java)
  }

  private var currentGameState = GameState.ENDED

  fun setGameState(state: GameState) {
    if (state.validPredecessor.contains(currentGameState)) {
      LOGGER.debug("Game state change: {} -> {}", currentGameState, state)
      currentGameState = state
    } else {
      throw InvalidGameStateException(
        "Gamestate $state can't be initiated from current state $currentGameState. Allowed predecessor " +
          "of $state are: ${state.validPredecessor.joinToString(",")}"
      )
    }
  }

  fun isInState(state: GameState): Boolean {
    return currentGameState == state
  }
}

enum class GameState {
  PREPARE,
  WAIT_FOR_ACTION,
  ACTION,
  ENDED;

  val validPredecessor: List<GameState>
    get() = when (this) {
      PREPARE -> listOf(ENDED)
      WAIT_FOR_ACTION -> listOf(PREPARE, ACTION)
      ACTION -> listOf(WAIT_FOR_ACTION)
      ENDED -> listOf(ACTION)
    }
}