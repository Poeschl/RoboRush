package xyz.poeschl.pathseeker.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import xyz.poeschl.pathseeker.controller.WebsocketController
import xyz.poeschl.pathseeker.exceptions.InsufficientFuelException
import xyz.poeschl.pathseeker.exceptions.PositionNotAllowedException
import xyz.poeschl.pathseeker.exceptions.PositionOutOfMapException
import xyz.poeschl.pathseeker.models.*

@Service
class RobotService(private val mapService: MapService, private val websocketController: WebsocketController) {
  companion object {
    private val LOGGER = LoggerFactory.getLogger(RobotService::class.java)
  }

  private val robots = mutableMapOf<Int, Robot>()
  private var robotIndex = 0

  /**
   * @throws PositionOutOfMapException if given position not within map bounds
   * @throws PositionNotAllowedException if given position is already occupied by another robot
   */
  fun createAndStoreRobot(fuel: Int, position: Position): Robot {
    checkPosition(position)
    val newIndex = robotIndex++
    val newRobot = Robot(newIndex, getRobotColor(), fuel, position)
    robots[newIndex] = newRobot
    return newRobot
  }

  fun getRobot(robotIndex: Int): Robot? {
    return robots[robotIndex]
  }

  fun getAllRobots(): List<Robot> {
    return robots.values.toList()
  }

  /**
   * @throws PositionOutOfMapException if new position after move not within map bounds
   * @throws PositionNotAllowedException if new position after move is already occupied by another robot
   */
  fun move(robot: Robot, direction: Direction): Position {
    val currentPosition = robot.position
    val newPosition =
      when (direction) {
        Direction.EAST -> currentPosition.eastPosition()
        Direction.WEST -> currentPosition.westPosition()
        Direction.NORTH -> currentPosition.northPosition()
        Direction.SOUTH -> currentPosition.southPosition()
      }

    checkPosition(newPosition)

    val fuelCost = mapService.getFuelCost(currentPosition, newPosition)
    if (fuelCost > robot.fuel) {
      throw InsufficientFuelException("The available fuel (${robot.fuel}) is insufficient for the journey. Required fuel: $fuelCost ")
    }

    robot.fuel -= fuelCost
    robot.position = newPosition
    LOGGER.debug("Moved robot {} from {}", robot, currentPosition)
    websocketController.sendRobotMoveUpdate(robot)

    return newPosition
  }

  fun scan(robot: Robot, distance: Int): List<Tile> {
    val scanResult = mapService.getTilesInDistance(robot.position, distance)
    val fuelCost = scanResult.second
    val tileList = scanResult.first

    if (fuelCost > robot.fuel) {
      throw InsufficientFuelException("The available fuel (${robot.fuel}) is insufficient for the requested scan. Required fuel: $fuelCost ")
    }

    robot.fuel -= fuelCost
    return tileList
  }

  private fun getRobotColor(): Color {
    return Color.randomColor()
  }

  private fun checkPosition(position: Position) {
    if (!mapService.isPositionValid(position)) {
      throw PositionOutOfMapException("Position $position is not in map bounds.")
    }
    if (isPositionOccupied(position)) {
      throw PositionNotAllowedException("Position $position is already occupied.")
    }
  }

  private fun isPositionOccupied(position: Position): Boolean {
    return getAllRobots().map { robot -> robot.position }.contains(position)
  }
}
