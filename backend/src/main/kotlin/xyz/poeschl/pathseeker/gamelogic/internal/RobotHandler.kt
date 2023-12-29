package xyz.poeschl.pathseeker.gamelogic.internal

import org.slf4j.LoggerFactory
import xyz.poeschl.pathseeker.configuration.GameLogic
import xyz.poeschl.pathseeker.exceptions.InvalidGameStateException
import xyz.poeschl.pathseeker.gamelogic.GameHandler
import xyz.poeschl.pathseeker.gamelogic.GameState
import xyz.poeschl.pathseeker.gamelogic.GameStatemachine
import xyz.poeschl.pathseeker.gamelogic.actions.RobotAction
import xyz.poeschl.pathseeker.models.*
import xyz.poeschl.pathseeker.repositories.Robot
import xyz.poeschl.pathseeker.repositories.RobotRepository

@GameLogic
class RobotHandler(
  private val robotRepository: RobotRepository,
  private val gameStateService: GameStatemachine
) {
  companion object {
    private val LOGGER = LoggerFactory.getLogger(RobotHandler::class.java)
    private const val DEFAULT_FUEL = 100
  }

  private val activeRobots = mutableListOf<ActiveRobot>()

  fun createRobot(): Robot {
    return robotRepository.save(Robot(null, Color.randomColor()))
  }

  fun registerRobotForGame(robotId: Long, startPosition: Position) {
    if (!gameStateService.isInState(GameState.PREPARE)) {
      throw InvalidGameStateException("Robot registration is only possible during 'Preparation' stage!")
    }

    if (activeRobots.none { it.id == robotId }) {
      val robot = robotRepository.findById(robotId)
      robot.ifPresent {
        isPositionOccupied(startPosition)
        val activeRobot = ActiveRobot(it.id!!, it.color, DEFAULT_FUEL, startPosition)
        activeRobots.add(activeRobot)
        LOGGER.info("Registered robot {}", robotId)
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
    activeRobots.first { it.id == robotId }.let { robot ->
      // If all checks are successful, the action will be saved
      nextAction.check(robot, gameHandler)
      robot.nextAction = nextAction
      LOGGER.info("Robot {} will do {} next", robot.id, nextAction)
    }
  }

  fun executeRobotMoves(gameHandler: GameHandler) {
    if (!gameStateService.isInState(GameState.ACTION)) {
      throw InvalidGameStateException("Actions are only allowed to be executed during 'Action' stage")
    }
    activeRobots.forEach { robot ->
      // Execute action for every robot with action
      val result = robot.nextAction?.action(robot, gameHandler)
      robot.nextAction = null

      if (result != null) {
        robot.lastResult = result
      }
    }
  }

  fun getActiveRobot(robotId: Long): ActiveRobot? {
    return activeRobots.first { it.id == robotId }
  }

  fun getAllActiveRobots(): List<ActiveRobot> {
    return activeRobots.toList()
  }

  fun getFirstNotOccupiedPosition(positions: List<Position>): Position? {
    val occupiedPositions = activeRobots.map { it.position }
    return positions.first { !occupiedPositions.contains(it) }
  }

  fun isPositionOccupied(position: Position): Boolean {
    return getAllActiveRobots().any { robot -> robot.position == position }
  }
}
