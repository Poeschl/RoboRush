package xyz.poeschl.roborush.gamelogic

import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import xyz.poeschl.roborush.configuration.GameLogic
import xyz.poeschl.roborush.models.settings.SettingKey.*
import xyz.poeschl.roborush.service.ConfigService
import kotlin.concurrent.thread

@GameLogic
class GameLoop(
  private val gameHandler: GameHandler,
  private val gameStateService: GameStateMachine,
  private val configService: ConfigService
) {
  companion object {
    private val LOGGER = LoggerFactory.getLogger(GameLoop::class.java)
  }

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
        Thread.sleep(configService.getDurationSetting(TIMEOUT_WAIT_FOR_PLAYERS).inWholeMilliseconds())
        if (gameHandler.getActiveRobots().isNotEmpty()) {
          // If there are registered robots we start the game
          gameStateService.setGameState(GameState.WAIT_FOR_ACTION)
        }
      }

      GameState.WAIT_FOR_ACTION -> {
        LOGGER.debug("Waiting for robot inputs")
        Thread.sleep(configService.getDurationSetting(TIMEOUT_WAIT_FOR_ACTION).inWholeMilliseconds())
        gameStateService.setGameState(GameState.ACTION)
      }

      GameState.ACTION -> {
        gameHandler.checkForIdleRound()

        if (gameHandler.isGameIdle()) {
          LOGGER.debug("No robot actions received.")
          gameStateService.setGameState(GameState.ENDED)
        } else {
          LOGGER.debug("Execute robot actions")
          gameHandler.executeAllRobotActions()

          val robotOnTarget = gameHandler.getActiveRobots().find { robot ->
            robot.position == gameHandler.getTargetPosition()
          }

          if (robotOnTarget != null) {
            gameHandler.endingRound(robotOnTarget)
            gameStateService.setGameState(GameState.ENDED)
          } else {
            gameStateService.setGameState(GameState.WAIT_FOR_ACTION)
          }
        }
      }

      GameState.ENDED -> {
        LOGGER.debug("Game ended")
        Thread.sleep(configService.getDurationSetting(TIMEOUT_GAME_END).inWholeMilliseconds())
        gameStateService.setGameState(GameState.PREPARE)
      }
    }
  }
}
