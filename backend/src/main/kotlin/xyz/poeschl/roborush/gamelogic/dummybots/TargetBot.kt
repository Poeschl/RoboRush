package xyz.poeschl.roborush.gamelogic.dummybots

import xyz.poeschl.roborush.exceptions.InsufficientFuelException
import xyz.poeschl.roborush.exceptions.PositionNotAllowedException
import xyz.poeschl.roborush.exceptions.PositionOutOfMapException
import xyz.poeschl.roborush.exceptions.TankFullException
import xyz.poeschl.roborush.gamelogic.GameHandler
import xyz.poeschl.roborush.gamelogic.actions.MoveAction
import xyz.poeschl.roborush.gamelogic.actions.SolarChargeAction
import xyz.poeschl.roborush.models.ActiveRobot
import xyz.poeschl.roborush.models.Direction
import xyz.poeschl.roborush.repositories.Robot

class TargetBot(private val gameHandler: GameHandler, robot: Robot) : Bot(gameHandler, robot) {

  override fun makeMove(activeRobot: ActiveRobot) {
    if (activeRobot.nextAction == null && activeRobot.fuel > 0) {
      var moveValid = false
      var triesLeft = 10

      while (!moveValid && triesLeft > 0) {
        try {
          val current = activeRobot.position
          val target = gameHandler.getTargetPosition()
          val attemptedMove = when {
            (current.x < target.x) -> MoveAction(Direction.EAST)
            (current.x > target.x) -> MoveAction(Direction.WEST)
            (current.y < target.y) -> MoveAction(Direction.SOUTH)
            (current.y > target.y) -> MoveAction(Direction.NORTH)
            else -> MoveAction(Direction.NORTH)
          }
          gameHandler.nextActionForRobot(activeRobot.id, attemptedMove)
          moveValid = true
        } catch (_: PositionNotAllowedException) {
        } catch (_: PositionOutOfMapException) {
          continue
        } catch (_: InsufficientFuelException) {
          try {
            gameHandler.nextActionForRobot(activeRobot.id, SolarChargeAction())
            moveValid = true
          } catch (_: TankFullException) {
          }
        }
        triesLeft--
      }
    }
  }
}
