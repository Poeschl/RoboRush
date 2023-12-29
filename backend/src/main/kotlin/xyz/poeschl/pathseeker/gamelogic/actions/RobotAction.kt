package xyz.poeschl.pathseeker.gamelogic.actions

import xyz.poeschl.pathseeker.gamelogic.internal.RobotHandler
import xyz.poeschl.pathseeker.models.ActiveRobot
import xyz.poeschl.pathseeker.models.Direction

interface RobotAction {

  /***
   * This method will check if the current action is allowed to execute.
   * If the action is not possible not an execution needs to be thrown!
   */
  fun check(robotHandler: RobotHandler, activeRobot: ActiveRobot)

  /***
   * Calling this method will execute the stored action immediately.
   */
  fun action(robotHandler: RobotHandler, activeRobot: ActiveRobot)
}

class Scan(private val scanDistance: Int) : RobotAction {
  override fun check(robotHandler: RobotHandler, activeRobot: ActiveRobot) {
    robotHandler.checkScan(activeRobot, scanDistance)
  }

  override fun action(robotHandler: RobotHandler, activeRobot: ActiveRobot) {
    robotHandler.scan(activeRobot, scanDistance)
  }

  override fun toString(): String {
    return "Scan(scanDistance=$scanDistance)"
  }
}

class Move(private val direction: Direction) : RobotAction {
  override fun check(robotHandler: RobotHandler, activeRobot: ActiveRobot) {
    robotHandler.checkMove(activeRobot, direction)
  }

  override fun action(robotHandler: RobotHandler, activeRobot: ActiveRobot) {
    robotHandler.move(activeRobot, direction)
  }

  override fun toString(): String {
    return "Move(direction=$direction)"
  }
}
