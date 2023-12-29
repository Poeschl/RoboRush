package xyz.poeschl.pathseeker.gamelogic

import org.slf4j.LoggerFactory
import xyz.poeschl.pathseeker.configuration.GameLogic
import xyz.poeschl.pathseeker.gamelogic.internal.MapHandler
import xyz.poeschl.pathseeker.gamelogic.internal.RobotHandler
import xyz.poeschl.pathseeker.models.Size
import java.time.Duration

@GameLogic
class GameLoop(
  private val mapHandler: MapHandler,
  private val robotHandler: RobotHandler,
  private val gameStateService: GameStatemachine
) {
  companion object {
    private val LOGGER = LoggerFactory.getLogger(GameLoop::class.java)
    private val PREPARATION_TIMEOUT = Duration.ofSeconds(2)
    private val WAIT_FOR_ACTION_TIMEOUT = Duration.ofSeconds(2)
  }

  fun startGame() {
    // Init play field and clear participants
    mapHandler.createNewRandomMap(Size(16, 8))
    robotHandler.clearActiveRobots()
    gameStateService.setGameState(GameState.PREPARE)

    // Wait for robots to participate
    Thread.sleep(PREPARATION_TIMEOUT.toMillis())
    gameStateService.setGameState(GameState.WAIT_FOR_ACTION)

    // Loop until game is finished
    while (!gameStateService.isInState(GameState.ENDED)) {
      if (gameStateService.isInState(GameState.WAIT_FOR_ACTION)) {
        // Game is waiting for all robot inputs
        LOGGER.debug("Waiting for robot inputs")
        Thread.sleep(WAIT_FOR_ACTION_TIMEOUT.toMillis())
        gameStateService.setGameState(GameState.ACTION)
      } else if (gameStateService.isInState(GameState.ACTION)) {
        // Game is execution all robot actions
        LOGGER.debug("Execute robot actions")
        robotHandler.executeRobotMoves()
        gameStateService.setGameState(GameState.WAIT_FOR_ACTION)

        // TODO: The goal check goes here
      }
    }

    gameStateService.setGameState(GameState.ENDED)
  }
}
