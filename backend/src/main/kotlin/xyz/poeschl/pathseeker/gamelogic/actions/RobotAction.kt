package xyz.poeschl.pathseeker.gamelogic.actions

import xyz.poeschl.pathseeker.gamelogic.GameHandler
import xyz.poeschl.pathseeker.models.ActiveRobot

/***
 * A action for the robot which the result of type T.
 */
interface RobotAction<T> {

  /***
   * This method will check if the current action is allowed to execute.
   * If the action is not possible not an execution needs to be thrown!
   */
  fun check(robot: ActiveRobot, gameHandler: GameHandler)

  /***
   * Calling this method will execute the stored action immediately.
   * @return Any result of the executed action.
   */
  fun action(robot: ActiveRobot, gameHandler: GameHandler): T

  override fun equals(other: Any?): Boolean
  override fun hashCode(): Int
}
