package xyz.poeschl.pathseeker.gamelogic

import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import xyz.poeschl.pathseeker.configuration.GameLogic
import java.time.Duration
import kotlin.concurrent.thread

@GameLogic
class GameLoop(
  private val gameHandler: GameHandler,
  private val gameStateService: GameStateMachine
) {
  companion object {
    private val LOGGER = LoggerFactory.getLogger(GameLoop::class.java)
    private val WAIT_FOR_PLAYERS_TIMEOUT = Duration.ofMinutes(3)
    private val WAIT_FOR_ACTION_TIMEOUT = Duration.ofSeconds(30)
    private val ENDED_GAME_TIMEOUT = Duration.ofMinutes(1)
    private const val NO_ACTION_END_THRESHOLD = 3
  }

  var noRobotActionCounter = 0

  @EventListener(ApplicationReadyEvent::class)
  fun startGameLoop() {
    thread(start = true, isDaemon = true) {
      gameStateService.setGameState(GameState.PREPARE)
      while (true) {
        gameLoop()
        // Prevent a CPU hell
        Thread.sleep(100)
      }
    }
  }

  fun gameLoop() {
    when (gameStateService.getCurrentState()) {
      GameState.PREPARE -> {
        gameHandler.prepareNewGame()
        gameStateService.setGameState(GameState.WAIT_FOR_PLAYERS)
      }

      GameState.WAIT_FOR_PLAYERS -> {
        Thread.sleep(WAIT_FOR_PLAYERS_TIMEOUT.toMillis())
        if (!gameHandler.getActiveRobots().isEmpty()) {
          // If there are registered robots we start the game
          gameStateService.setGameState(GameState.WAIT_FOR_ACTION)
        }
      }

      GameState.WAIT_FOR_ACTION -> {
        LOGGER.debug("Waiting for robot inputs")
        Thread.sleep(WAIT_FOR_ACTION_TIMEOUT.toMillis())
        gameStateService.setGameState(GameState.ACTION)
      }

      GameState.ACTION -> {
        if (!gameHandler.robotMovesReceived()) {
          noRobotActionCounter++
        }

        // TODO: Add target tile check
        if (noRobotActionCounter >= NO_ACTION_END_THRESHOLD) {
          LOGGER.debug("No robot actions received.")
          gameStateService.setGameState(GameState.ENDED)
        } else {
          LOGGER.debug("Execute robot actions")
          gameHandler.executeAllRobotMoves()
          gameStateService.setGameState(GameState.WAIT_FOR_ACTION)
        }
      }

      GameState.ENDED -> {
        LOGGER.debug("Game ended")
        Thread.sleep(ENDED_GAME_TIMEOUT.toMillis())
        gameStateService.setGameState(GameState.PREPARE)
      }
    }
  }
}
