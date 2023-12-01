package xyz.poeschl.pathseeker.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import xyz.poeschl.pathseeker.exceptions.InsufficientFuelException
import xyz.poeschl.pathseeker.exceptions.MoveOutOfMapException
import xyz.poeschl.pathseeker.models.Direction
import xyz.poeschl.pathseeker.models.Position
import xyz.poeschl.pathseeker.models.Robot
import xyz.poeschl.pathseeker.models.Tile

@Service
class RobotService(private val mapService: MapService) {
  companion object {
    private val LOGGER = LoggerFactory.getLogger(RobotService::class.java)
  }

  val robots = mutableMapOf<Int, Robot>()
  var robotIndex = 0

  fun createAndStoreRobot(fuel: Int, position: Position): Robot {
    val newIndex = robotIndex++
    val newRobot = Robot(newIndex, fuel, position)
    robots[newIndex] = newRobot
    return newRobot
  }

  fun getRobot(robotIndex: Int): Robot? {
    return robots[robotIndex]
  }

  fun move(robot: Robot, direction: Direction): Position {
    val currentPosition = robot.position
    val newPosition =
      when (direction) {
        Direction.EAST -> currentPosition.eastPosition()
        Direction.WEST -> currentPosition.westPosition()
        Direction.NORTH -> currentPosition.northPosition()
        Direction.SOUTH -> currentPosition.southPosition()
      }

    if (!mapService.isPositionValid(newPosition)) {
      throw MoveOutOfMapException("New position $newPosition is not in map bounds.")
    }

    val fuelCost = mapService.getFuelCost(currentPosition, newPosition)
    if (fuelCost > robot.fuel) {
      throw InsufficientFuelException("The available fuel (${robot.fuel}) is insufficient for the journey. Required fuel: $fuelCost ")
    }

    robot.fuel -= fuelCost
    robot.position = newPosition
    LOGGER.debug("Moved robot {} from {}", robot, currentPosition)

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
}
