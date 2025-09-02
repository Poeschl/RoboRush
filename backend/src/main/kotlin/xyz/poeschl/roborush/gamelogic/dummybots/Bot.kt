package xyz.poeschl.roborush.gamelogic.dummybots

import xyz.poeschl.roborush.exceptions.InsufficientFuelException
import xyz.poeschl.roborush.exceptions.PositionNotAllowedException
import xyz.poeschl.roborush.exceptions.PositionOutOfMapException
import xyz.poeschl.roborush.gamelogic.GameHandler
import xyz.poeschl.roborush.gamelogic.GameState
import xyz.poeschl.roborush.gamelogic.actions.MoveAction
import xyz.poeschl.roborush.models.ActiveRobot
import xyz.poeschl.roborush.models.Direction
import xyz.poeschl.roborush.models.Position
import xyz.poeschl.roborush.repositories.Robot

open class Bot(private val gameHandler: GameHandler, private val robot: Robot) {

  companion object {
    const val PARTICIPATE_IF_LESS_THEN_ACTIVE = 5
  }

  private var participating = false
  private var lastThreePositions: MutableList<Position> = mutableListOf()

  fun getId(): Long? = robot.id

  fun doSomething(gameState: GameState) {
    when (gameState) {
      GameState.WAIT_FOR_PLAYERS -> participateInGameIfNeeded()
      GameState.WAIT_FOR_ACTION -> {
        val activeRobot = gameHandler.getActiveRobot(robot.id!!)
        if (activeRobot != null) {
          makeMove(activeRobot)
        }
      }
      GameState.ENDED -> reset()
      else -> {}
    }
  }

  open fun makeMove(activeRobot: ActiveRobot) {
    if (activeRobot.nextAction == null && activeRobot.fuel > 0) {
      var moveValid = false
      var triesLeft = 10

      while (!moveValid && triesLeft > 0) {
        try {
          val attemptedMove = MoveAction(Direction.entries.random())
          val newPosition = attemptedMove.getResultPosition(activeRobot.position)

          if (!lastThreePositions.contains(newPosition)) {
            rememberPosition(activeRobot.position)
            gameHandler.nextActionForRobot(activeRobot.id, attemptedMove)
            moveValid = true
          } else {
            continue
          }
        } catch (_: PositionNotAllowedException) {
        } catch (_: PositionOutOfMapException) {
        } catch (_: InsufficientFuelException) {
        }
        triesLeft--
      }
    }
  }

  open fun reset() {
    participating = false
    lastThreePositions.clear()
  }

  private fun participateInGameIfNeeded() {
    if (!participating && gameHandler.getActiveRobots().size < PARTICIPATE_IF_LESS_THEN_ACTIVE) {
      gameHandler.registerRobotForNextGame(robot.id!!)
      participating = true
    }
  }

  private fun rememberPosition(position: Position) {
    lastThreePositions.addFirst(position)
    if (lastThreePositions.size > 3) {
      lastThreePositions.removeLast()
    }
  }
}
