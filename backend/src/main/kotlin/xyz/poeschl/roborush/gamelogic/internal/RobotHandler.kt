package xyz.poeschl.roborush.gamelogic.internal

import org.slf4j.LoggerFactory
import xyz.poeschl.roborush.configuration.GameLogic
import xyz.poeschl.roborush.exceptions.GameStateException
import xyz.poeschl.roborush.gamelogic.GameHandler
import xyz.poeschl.roborush.gamelogic.GameState
import xyz.poeschl.roborush.gamelogic.GameStateMachine
import xyz.poeschl.roborush.gamelogic.actions.MoveAction
import xyz.poeschl.roborush.gamelogic.actions.RobotAction
import xyz.poeschl.roborush.models.ActiveRobot
import xyz.poeschl.roborush.models.Position
import xyz.poeschl.roborush.repositories.RobotRepository

@GameLogic
class RobotHandler(
  private val robotRepository: RobotRepository,
  private val gameStateService: GameStateMachine
) {

  companion object {
    private val LOGGER = LoggerFactory.getLogger(RobotHandler::class.java)
  }

  private var initialRobotMaxFuel = 0

  private val activeRobots = mutableSetOf<ActiveRobot>()

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
          activeRobot = ActiveRobot(realRobot.id!!, realRobot.user.username, realRobot.user, realRobot.color, initialRobotMaxFuel, startPosition)
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
  }

  fun setNextMove(robotId: Long, gameHandler: GameHandler, nextAction: RobotAction<*>): ActiveRobot? {
    if (!gameStateService.isInState(GameState.WAIT_FOR_ACTION)) {
      throw GameStateException("Sending the next robot move is only allowed during 'Waiting for action' stage!")
    }
    activeRobots.firstOrNull { it.id == robotId }?.let { robot ->
      // If all checks are successful, the action will be saved
      nextAction.check(robot, gameHandler)
      robot.nextAction = nextAction
      LOGGER.info("Robot {} will do {} next", robot.id, nextAction)
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
      val result = robot.nextAction?.action(robot, gameHandler)
      robot.nextAction = null
      robot.lastResult = result
    }
  }

  fun countPendingRobotActions(): Int {
    return activeRobots.map { it.nextAction }.count { it != null }
  }

  fun getActiveRobot(robotId: Long): ActiveRobot? {
    return activeRobots.firstOrNull { it.id == robotId }
  }

  fun getAllActiveRobots(): Set<ActiveRobot> {
    return activeRobots.toSet()
  }

  fun getACurrentlyFreePosition(positions: List<Position>): Position? {
    val occupiedPositions = activeRobots.map { it.position }
    return positions.filter { !occupiedPositions.contains(it) }.randomOrNull()
  }

  fun isPositionCurrentFree(position: Position): Boolean {
    return activeRobots.none { robot -> robot.position == position }
  }

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

  fun wonTheCurrentRound(robot: ActiveRobot) {
    val actualRobot = robotRepository.findById(robot.id)
    actualRobot.ifPresent {
      it.score += 1
      robotRepository.save(it)
    }
  }
}
