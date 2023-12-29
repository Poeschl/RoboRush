package xyz.poeschl.pathseeker.gamelogic.internal

import org.slf4j.LoggerFactory
import xyz.poeschl.pathseeker.configuration.GameLogic
import xyz.poeschl.pathseeker.controller.WebsocketController
import xyz.poeschl.pathseeker.exceptions.InsufficientFuelException
import xyz.poeschl.pathseeker.exceptions.InvalidGameStateException
import xyz.poeschl.pathseeker.exceptions.PositionNotAllowedException
import xyz.poeschl.pathseeker.exceptions.PositionOutOfMapException
import xyz.poeschl.pathseeker.gamelogic.GameState
import xyz.poeschl.pathseeker.gamelogic.GameStatemachine
import xyz.poeschl.pathseeker.gamelogic.actions.RobotAction
import xyz.poeschl.pathseeker.models.*
import xyz.poeschl.pathseeker.repositories.Robot
import xyz.poeschl.pathseeker.repositories.RobotRepository

@GameLogic
class RobotHandler(
  private val mapHandler: MapHandler,
  private val websocketController: WebsocketController,
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
        checkPosition(startPosition)
        val activeRobot = ActiveRobot(it.id!!, it.color, DEFAULT_FUEL, startPosition)
        activeRobots.add(activeRobot)
        LOGGER.info("Registered robot {}", robotId)
      }
    }
  }

  fun clearActiveRobots() {
    activeRobots.clear()
  }

  fun setNextMove(robotId: Long, nextAction: RobotAction) {
    if (!gameStateService.isInState(GameState.WAIT_FOR_ACTION)) {
      throw InvalidGameStateException("Sending the next robot move is only allowed during 'Waiting for action' stage!")
    }
    activeRobots.first { it.id == robotId }.let { robot ->
      // If all checks are successful, the action will be saved
      nextAction.check(this, robot)
      robot.nextAction = nextAction
      LOGGER.info("Robot {} will do {} next", robot.id, nextAction)
    }
  }

  fun executeRobotMoves() {
    if (!gameStateService.isInState(GameState.ACTION)) {
      throw InvalidGameStateException("Actions are only allowed to be executed during 'Action' stage")
    }
    activeRobots.forEach { robot ->
      // Execute action for every robot with action
      robot.nextAction?.action(this, robot)
      robot.nextAction = null
    }
  }

  fun getActiveRobot(robotId: Long): ActiveRobot? {
    return activeRobots.first { it.id == robotId }
  }

  fun getAllActiveRobots(): List<ActiveRobot> {
    return activeRobots.toList()
  }

  /**
   * @throws PositionOutOfMapException if new position after move not within map bounds
   * @throws PositionNotAllowedException if new position after move is already occupied by another robot
   */
  fun checkMove(robot: ActiveRobot, direction: Direction) {
    val currentPosition = robot.position
    val newPosition =
      when (direction) {
        Direction.EAST -> currentPosition.eastPosition()
        Direction.WEST -> currentPosition.westPosition()
        Direction.NORTH -> currentPosition.northPosition()
        Direction.SOUTH -> currentPosition.southPosition()
      }

    LOGGER.debug("Check {} -> {}", currentPosition, newPosition)
    checkPosition(newPosition)

    val fuelCost = mapHandler.getFuelCost(currentPosition, newPosition)
    if (fuelCost > robot.fuel) {
      throw InsufficientFuelException("The available fuel (${robot.fuel}) is insufficient for the journey. Required fuel: $fuelCost ")
    }
  }

  fun move(robot: ActiveRobot, direction: Direction): Position {
    val currentPosition = robot.position
    val newPosition =
      when (direction) {
        Direction.EAST -> currentPosition.eastPosition()
        Direction.WEST -> currentPosition.westPosition()
        Direction.NORTH -> currentPosition.northPosition()
        Direction.SOUTH -> currentPosition.southPosition()
      }
    LOGGER.debug("{} -> {}", currentPosition, newPosition)

    val fuelCost = mapHandler.getFuelCost(currentPosition, newPosition)

    robot.fuel -= fuelCost
    robot.position = newPosition
    LOGGER.debug("Moved robot {} {} -> {}", robot.id, currentPosition, newPosition)
    websocketController.sendRobotMoveUpdate(robot)

    return newPosition
  }

  fun checkScan(robot: ActiveRobot, distance: Int) {
    val scanResult = mapHandler.getTilesInDistance(robot.position, distance)
    val fuelCost = scanResult.second

    if (fuelCost > robot.fuel) {
      throw InsufficientFuelException("The available fuel (${robot.fuel}) is insufficient for the requested scan. Required fuel: $fuelCost ")
    }
  }

  fun scan(robot: ActiveRobot, distance: Int): List<Tile> {
    val scanResult = mapHandler.getTilesInDistance(robot.position, distance)
    val fuelCost = scanResult.second
    val tileList = scanResult.first

    robot.fuel -= fuelCost
    return tileList
  }

  fun getFirstNotOccupiedPosition(positions: List<Position>): Position? {
    val occupiedPositions = activeRobots.map { it.position }
    return positions.first { !occupiedPositions.contains(it) }
  }

  private fun checkPosition(position: Position) {
    if (!mapHandler.isPositionValid(position)) {
      throw PositionOutOfMapException("Position $position is not in map bounds.")
    }
    if (isPositionOccupied(position)) {
      throw PositionNotAllowedException("Position $position is already occupied.")
    }
  }

  private fun isPositionOccupied(position: Position): Boolean {
    return getAllActiveRobots().map { robot -> robot.position }.contains(position)
  }
}
