package xyz.poeschl.roborush.gamelogic

import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import xyz.poeschl.roborush.configuration.GameLogic
import xyz.poeschl.roborush.models.ActiveRobot
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

  private var noRobotActionCounter = 0
  private var winningRobot: ActiveRobot? = null

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
        winningRobot = null
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
        if (!gameHandler.robotMovesReceived()) {
          noRobotActionCounter++
        } else {
          noRobotActionCounter = 0
        }

        // TODO: Add target tile check
        if (noRobotActionCounter >= configService.getIntSetting(THRESHOLD_NO_ROBOT_ACTION_END_GAME).value) {
          LOGGER.debug("No robot actions received.")
          gameStateService.setGameState(GameState.ENDED)
        } else {
          LOGGER.debug("Execute robot actions")
          gameHandler.executeAllRobotMoves()

          gameHandler.getActiveRobots().forEach { robot ->
            if (robot.position == gameHandler.getTargetPosition()) {
              winningRobot = robot
              gameStateService.setGameState(GameState.ENDED)
            }
          }
          if (winningRobot == null) {
            gameStateService.setGameState(GameState.WAIT_FOR_ACTION)
          }
        }
      }

      GameState.ENDED -> {
        LOGGER.debug("Game ended")
        if (winningRobot != null) {
          LOGGER.debug("Robot ${winningRobot!!.name} has reached the target tile!")
          gameHandler.wonTheCurrentRound(winningRobot!!)
        } else {
          LOGGER.debug("Noone reached the target tile in time!")
        }
        Thread.sleep(configService.getDurationSetting(TIMEOUT_GAME_END).inWholeMilliseconds())
        gameStateService.setGameState(GameState.PREPARE)
      }
    }
  }
}
