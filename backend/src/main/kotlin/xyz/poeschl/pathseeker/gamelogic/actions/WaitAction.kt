package xyz.poeschl.pathseeker.gamelogic.actions

import com.fasterxml.jackson.annotation.JsonCreator
import org.slf4j.LoggerFactory
import xyz.poeschl.pathseeker.gamelogic.GameHandler
import xyz.poeschl.pathseeker.models.ActiveRobot
import xyz.poeschl.pathseeker.models.Position

class WaitAction @JsonCreator constructor() : RobotAction<Position> {

  companion object {
    private val LOGGER = LoggerFactory.getLogger(WaitAction::class.java)
  }

  override fun check(robot: ActiveRobot, gameHandler: GameHandler) {
    // Waiting is always successful
  }

  override fun action(robot: ActiveRobot, gameHandler: GameHandler): Position {
    // Doing nothing
    return robot.position
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    return true
  }

  override fun hashCode(): Int {
    return javaClass.hashCode()
  }

  override fun toString(): String {
    return "Wait()"
  }
}
