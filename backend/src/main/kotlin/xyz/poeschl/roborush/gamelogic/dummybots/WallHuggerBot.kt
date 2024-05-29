package xyz.poeschl.roborush.gamelogic.dummybots

import xyz.poeschl.roborush.exceptions.InsufficientFuelException
import xyz.poeschl.roborush.exceptions.PositionNotAllowedException
import xyz.poeschl.roborush.exceptions.PositionOutOfMapException
import xyz.poeschl.roborush.gamelogic.GameHandler
import xyz.poeschl.roborush.gamelogic.actions.MoveAction
import xyz.poeschl.roborush.models.ActiveRobot
import xyz.poeschl.roborush.models.Direction
import xyz.poeschl.roborush.repositories.Robot
import kotlin.random.Random

class WallHuggerBot(private val gameHandler: GameHandler, robot: Robot) : Bot(gameHandler, robot) {

  companion object {
    private val directions = listOf(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
  }

  private var indexCounter = Random.nextInt(3)

  override fun makeMove(activeRobot: ActiveRobot) {
    if (activeRobot.nextAction == null && activeRobot.fuel > 0) {
      var moveValid = false
      var triesLeft = 10

      while (!moveValid && triesLeft > 0) {
        try {
          val attemptedMove = MoveAction(directions[indexCounter % Direction.entries.size])
          gameHandler.nextActionForRobot(activeRobot.id, attemptedMove)
          moveValid = true
        } catch (_: PositionNotAllowedException) {
        } catch (_: PositionOutOfMapException) {
          indexCounter++
          continue
        } catch (_: InsufficientFuelException) {
        }
        triesLeft--
      }
    }
  }

  override fun reset() {
    super.reset()
    indexCounter = Random.nextInt(3)
  }
}
