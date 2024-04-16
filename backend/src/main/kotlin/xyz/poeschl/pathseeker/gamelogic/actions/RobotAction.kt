package xyz.poeschl.pathseeker.gamelogic.actions

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import xyz.poeschl.pathseeker.gamelogic.GameHandler
import xyz.poeschl.pathseeker.models.ActiveRobot

/***
 * A action for the robot which the result of type T.
 */
@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "type"
)
@JsonSubTypes(
  Type(value = MoveAction::class, name = "move"),
  Type(value = ScanAction::class, name = "scan"),
  Type(value = WaitAction::class, name = "wait")
)
interface RobotAction<T> {

  /***
   * This method will check if the current action is allowed to execute.
   * If the action is not possible not an execution needs to be thrown!
   */
  abstract fun check(robot: ActiveRobot, gameHandler: GameHandler)

  /***
   * Calling this method will execute the stored action immediately.
   * @return Any result of the executed action.
   */
  abstract fun action(robot: ActiveRobot, gameHandler: GameHandler): T

  abstract override fun equals(other: Any?): Boolean
  abstract override fun hashCode(): Int
}
