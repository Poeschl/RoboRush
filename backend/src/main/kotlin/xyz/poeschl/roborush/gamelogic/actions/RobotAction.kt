package xyz.poeschl.roborush.gamelogic.actions

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import xyz.poeschl.roborush.gamelogic.GameHandler
import xyz.poeschl.roborush.models.ActiveRobot

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
  Type(value = WaitAction::class, name = "wait"),
  Type(value = RefuelAction::class, name = "refuel"),
  Type(value = SolarChargeAction::class, name = "solarCharge"),
  Type(value = FullMapScanAction::class, name = "fullScan")
)
interface RobotAction<T : Result> {

  /***
   * This method will check if the current action is allowed to execute.
   * If the action is not possible not an execution needs to be thrown!
   */
  fun check(robot: ActiveRobot, gameHandler: GameHandler)

  /***
   * Calling this method will execute the stored action immediately.
   * @return Any result of the executed action.
   */
  fun action(robot: ActiveRobot, gameHandler: GameHandler): RobotActionResult<T>

  override fun equals(other: Any?): Boolean
  override fun hashCode(): Int
}

data class RobotActionResult<T>(val updatedRobot: ActiveRobot, val result: T)

interface Result
