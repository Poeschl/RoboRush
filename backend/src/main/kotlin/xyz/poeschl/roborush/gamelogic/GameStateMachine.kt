package xyz.poeschl.roborush.gamelogic

import org.slf4j.LoggerFactory
import xyz.poeschl.roborush.configuration.GameLogic
import xyz.poeschl.roborush.controller.WebsocketController
import xyz.poeschl.roborush.exceptions.GameStateException

@GameLogic
class GameStateMachine(private val websocketController: WebsocketController) {

  companion object {
    private val LOGGER = LoggerFactory.getLogger(GameStateMachine::class.java)
  }

  private var currentGameState = GameState.ENDED

  fun setGameState(state: GameState) {
    if (state.validPredecessor.contains(currentGameState)) {
      LOGGER.debug("Game state change: {} -> {}", currentGameState, state)
      currentGameState = state
      websocketController.sendGameStateUpdate(currentGameState)
    } else {
      throw GameStateException(
        "Gamestate $state can't be initiated from current state $currentGameState. Allowed predecessor " +
          "of $state are: ${state.validPredecessor.joinToString(",")}"
      )
    }
  }

  fun isInState(state: GameState): Boolean {
    return currentGameState == state
  }

  fun getCurrentState(): GameState {
    return currentGameState
  }
}

enum class GameState {
  PREPARE,
  WAIT_FOR_PLAYERS,
  WAIT_FOR_ACTION,
  ACTION,
  ENDED;

  val validPredecessor: List<GameState>
    get() = when (this) {
      PREPARE -> listOf(ENDED)
      WAIT_FOR_PLAYERS -> listOf(PREPARE)
      WAIT_FOR_ACTION -> listOf(WAIT_FOR_PLAYERS, ACTION)
      ACTION -> listOf(WAIT_FOR_ACTION)
      ENDED -> listOf(ACTION)
    }
}
