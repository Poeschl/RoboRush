package xyz.poeschl.roborush.gamelogic.dummybots

import xyz.poeschl.roborush.gamelogic.GameHandler
import xyz.poeschl.roborush.models.ActiveRobot
import xyz.poeschl.roborush.repositories.Robot

class ChillerBot(gameHandler: GameHandler, robot: Robot) : Bot(gameHandler, robot) {

  override fun makeMove(activeRobot: ActiveRobot) {
    // Just do nothing
  }
}
