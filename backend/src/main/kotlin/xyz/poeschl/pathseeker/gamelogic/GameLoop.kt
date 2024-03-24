package xyz.poeschl.pathseeker.gamelogic

import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import xyz.poeschl.pathseeker.configuration.GameLogic
import xyz.poeschl.pathseeker.service.ConfigService
import kotlin.concurrent.thread
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@GameLogic
class GameLoop(
  private val gameHandler: GameHandler,
  private val gameStateService: GameStateMachine,
  private val configService: ConfigService
) {
  companion object {
    private val LOGGER = LoggerFactory.getLogger(GameLoop::class.java)
    private val WAIT_FOR_PLAYERS_TIMEOUT = 3.minutes
    private val WAIT_FOR_ACTION_TIMEOUT = 30.seconds
    private val ENDED_GAME_TIMEOUT = 2.minutes
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
        Thread.sleep(WAIT_FOR_PLAYERS_TIMEOUT.inWholeMilliseconds)
        if (!gameHandler.getActiveRobots().isEmpty()) {
          // If there are registered robots we start the game
          gameStateService.setGameState(GameState.WAIT_FOR_ACTION)
        }
      }

      GameState.WAIT_FOR_ACTION -> {
        LOGGER.debug("Waiting for robot inputs")
        Thread.sleep(WAIT_FOR_ACTION_TIMEOUT.inWholeMilliseconds)
        gameStateService.setGameState(GameState.ACTION)
      }

      GameState.ACTION -> {
        if (!gameHandler.robotMovesReceived()) {
          noRobotActionCounter++
        } else {
          noRobotActionCounter = 0
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
        Thread.sleep(ENDED_GAME_TIMEOUT.inWholeMilliseconds)
        gameStateService.setGameState(GameState.PREPARE)
      }
    }
  }
}
