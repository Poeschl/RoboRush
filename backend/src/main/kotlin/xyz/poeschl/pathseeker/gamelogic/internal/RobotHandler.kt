package xyz.poeschl.pathseeker.gamelogic.internal

import org.slf4j.LoggerFactory
import xyz.poeschl.pathseeker.configuration.GameLogic
import xyz.poeschl.pathseeker.exceptions.InvalidGameStateException
import xyz.poeschl.pathseeker.gamelogic.GameHandler
import xyz.poeschl.pathseeker.gamelogic.GameState
import xyz.poeschl.pathseeker.gamelogic.GameStateMachine
import xyz.poeschl.pathseeker.gamelogic.actions.MoveAction
import xyz.poeschl.pathseeker.gamelogic.actions.RobotAction
import xyz.poeschl.pathseeker.models.ActiveRobot
import xyz.poeschl.pathseeker.models.Position
import xyz.poeschl.pathseeker.repositories.RobotRepository

@GameLogic
class RobotHandler(
  private val robotRepository: RobotRepository,
  private val gameStateService: GameStateMachine
) {

  companion object {
    private val LOGGER = LoggerFactory.getLogger(RobotHandler::class.java)
    private const val DEFAULT_FUEL = 100
  }

  private val activeRobots = mutableSetOf<ActiveRobot>()

  fun registerRobotForGame(robotId: Long, startPosition: Position) {
    if (!gameStateService.isInState(GameState.PREPARE)) {
      throw InvalidGameStateException("Robot registration is only possible during 'Preparation' stage!")
    }

    if (activeRobots.none { it.id == robotId }) {
      val robot = robotRepository.findById(robotId)
      robot.ifPresent {
        if (isPositionCurrentFree(startPosition)) {
          val activeRobot = ActiveRobot(it.id!!, it.color, DEFAULT_FUEL, startPosition)
          activeRobots.add(activeRobot)
          LOGGER.info("Registered robot {}", robotId)
        } else {
          LOGGER.error("Could not register robot {}, its start position is occupied", robotId)
        }
      }
    }
  }

  fun clearActiveRobots() {
    activeRobots.clear()
  }

  fun setNextMove(robotId: Long, gameHandler: GameHandler, nextAction: RobotAction<*>) {
    if (!gameStateService.isInState(GameState.WAIT_FOR_ACTION)) {
      throw InvalidGameStateException("Sending the next robot move is only allowed during 'Waiting for action' stage!")
    }
    activeRobots.firstOrNull { it.id == robotId }?.let { robot ->
      // If all checks are successful, the action will be saved
      nextAction.check(robot, gameHandler)
      robot.nextAction = nextAction
      LOGGER.info("Robot {} will do {} next", robot.id, nextAction)
    }
  }

  fun executeRobotActions(gameHandler: GameHandler) {
    if (!gameStateService.isInState(GameState.ACTION)) {
      throw InvalidGameStateException("Actions are only allowed to be executed during 'Action' stage")
    }
    activeRobots.forEach { robot ->
      // Execute action for every robot with action
      val result = robot.nextAction?.action(robot, gameHandler)
      robot.nextAction = null
      robot.lastResult = result
    }
  }

  fun getActiveRobot(robotId: Long): ActiveRobot? {
    return activeRobots.firstOrNull { it.id == robotId }
  }

  fun getAllActiveRobots(): Set<ActiveRobot> {
    return activeRobots.toSet()
  }

  fun getFirstCurrentlyFreePosition(positions: List<Position>): Position? {
    val occupiedPositions = activeRobots.map { it.position }
    return positions.firstOrNull { !occupiedPositions.contains(it) }
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
}
