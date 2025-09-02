package xyz.poeschl.roborush.gamelogic.actions

import com.fasterxml.jackson.annotation.JsonCreator
import org.slf4j.LoggerFactory
import xyz.poeschl.roborush.gamelogic.GameHandler
import xyz.poeschl.roborush.gamelogic.actions.WaitAction.*
import xyz.poeschl.roborush.models.ActiveRobot

class WaitAction @JsonCreator constructor() : RobotAction<EmptyResult> {

  companion object {
    private val LOGGER = LoggerFactory.getLogger(WaitAction::class.java)
  }

  override fun check(robot: ActiveRobot, gameHandler: GameHandler) {
    // Waiting is always successful
  }

  override fun action(robot: ActiveRobot, gameHandler: GameHandler): RobotActionResult<EmptyResult> {
    // Doing nothing
    return RobotActionResult(robot, EmptyResult())
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    return true
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String = "Wait()"

  data class EmptyResult(val state: String = "(╯°□°)╯︵ ┻━┻") : Result
}
