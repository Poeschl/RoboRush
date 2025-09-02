package xyz.poeschl.roborush.gamelogic.internal

import org.slf4j.LoggerFactory
import xyz.poeschl.roborush.configuration.GameLogic
import xyz.poeschl.roborush.exceptions.GameStateException
import xyz.poeschl.roborush.gamelogic.GameHandler
import xyz.poeschl.roborush.gamelogic.GameState
import xyz.poeschl.roborush.gamelogic.GameStateMachine
import xyz.poeschl.roborush.gamelogic.actions.MoveAction
import xyz.poeschl.roborush.gamelogic.actions.RobotAction
import xyz.poeschl.roborush.gamelogic.actions.WaitAction
import xyz.poeschl.roborush.models.ActiveRobot
import xyz.poeschl.roborush.models.Position
import xyz.poeschl.roborush.repositories.Robot
import xyz.poeschl.roborush.repositories.RobotRepository

@GameLogic
class RobotHandler(
  // Should be a service as well, but difficult because of a dependency loop
  private val robotRepository: RobotRepository,
  private val gameStateService: GameStateMachine
) {

  companion object {
    private val LOGGER = LoggerFactory.getLogger(RobotHandler::class.java)
  }

  private var initialRobotMaxFuel = 0

  private val activeRobots = mutableListOf<ActiveRobot>()

  private var winningRobot: Robot? = null

  fun getWinningRobot() = winningRobot

  fun registerRobotForGame(robotId: Long, startPosition: Position): ActiveRobot? {
    if (!gameStateService.isInState(GameState.WAIT_FOR_PLAYERS)) {
      throw GameStateException("Robot registration is only possible during 'Preparation' stage!")
    }

    var activeRobot: ActiveRobot? = null
    if (activeRobots.none { it.id == robotId }) {
      val robot = robotRepository.findById(robotId)
      if (robot.isPresent) {
        val realRobot = robot.get()
        if (isPositionCurrentFree(startPosition)) {
          activeRobot =
            ActiveRobot(realRobot.id!!, realRobot.user.username, realRobot.user, realRobot.color, initialRobotMaxFuel, initialRobotMaxFuel, startPosition)
          activeRobots.add(activeRobot)
          LOGGER.info("Registered robot {}", robotId)
        } else {
          LOGGER.error("Could not register robot {}, its start position is occupied", robotId)
        }
      }
    }
    return activeRobot
  }

  fun setRobotMaxFuel(maxFuel: Int) {
    this.initialRobotMaxFuel = maxFuel
  }

  fun clearActiveRobots() {
    activeRobots.clear()
    winningRobot = null
  }

  fun setNextMove(robotId: Long, gameHandler: GameHandler, nextAction: RobotAction<*>): ActiveRobot? {
    if (!gameStateService.isInState(GameState.WAIT_FOR_ACTION)) {
      throw GameStateException("Sending the next robot move is only allowed during 'Waiting for action' stage!")
    }
    activeRobots.firstOrNull { it.id == robotId }?.let { robot ->
      // If all checks are successful, the action will be saved
      nextAction.check(robot, gameHandler)
      robot.nextAction = nextAction
      updateActiveRobot(robot)
      LOGGER.debug("Robot {} will do {} next", robot.id, nextAction)
      return robot
    }
    return null
  }

  fun executeRobotActions(gameHandler: GameHandler) {
    if (!gameStateService.isInState(GameState.ACTION)) {
      throw GameStateException("Actions are only allowed to be executed during 'Action' stage")
    }
    activeRobots.forEach { robot ->
      // Execute action for every robot with action
      val actionResult = robot.nextAction?.action(robot, gameHandler)

      if (actionResult != null) {
        robot.nextAction = null
        robot.lastResult = actionResult.result
        updateActiveRobot(robot)
      }
    }
  }

  fun isEveryRobotIdle(): Boolean = activeRobots.none { it.nextAction != null && it.nextAction !is WaitAction }

  fun getActiveRobot(robotId: Long): ActiveRobot? {
    // Return a copy of the real robot to avoid reference side effects
    return activeRobots.firstOrNull { it.id == robotId }?.copy()
  }

  fun getAllActiveRobots(): Set<ActiveRobot> = activeRobots.toSet()

  private fun updateActiveRobot(activeRobot: ActiveRobot) {
    // Replace the existing active robot
    val replaceIndex = activeRobots.indexOfFirst { it.id == activeRobot.id }
    activeRobots[replaceIndex] = activeRobot
  }

  fun getACurrentlyFreePosition(positions: List<Position>): Position? {
    val occupiedPositions = activeRobots.map { it.position }
    return positions.filter { !occupiedPositions.contains(it) }.randomOrNull()
  }

  fun isPositionCurrentFree(position: Position): Boolean = activeRobots.none { robot -> robot.position == position }

  fun isPositionFreeAfterActions(position: Position): Boolean {
    val movingRobots = activeRobots.filter { it.nextAction is MoveAction }
    val nonMovingRobots = activeRobots.filter { !movingRobots.contains(it) }

    val positionsAfterNextActions = mutableSetOf<Position>()
    movingRobots
      .map { (it.nextAction as MoveAction).getResultPosition(it.position) }
      .forEach { positionsAfterNextActions.add(it) }
    nonMovingRobots
      .map { it.position }
      .forEach { positionsAfterNextActions.add(it) }

    return positionsAfterNextActions.none { it == position }
  }

  fun setRoundWinner(robot: ActiveRobot) {
    val actualRobot = robotRepository.findById(robot.id)
    actualRobot.ifPresent {
      winningRobot = it
    }
  }
}
