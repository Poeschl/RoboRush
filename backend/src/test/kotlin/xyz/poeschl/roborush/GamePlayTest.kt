package xyz.poeschl.roborush

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import xyz.poeschl.roborush.controller.WebsocketController
import xyz.poeschl.roborush.gamelogic.GameHandler
import xyz.poeschl.roborush.gamelogic.GameState
import xyz.poeschl.roborush.gamelogic.GameStateMachine
import xyz.poeschl.roborush.gamelogic.actions.MoveAction
import xyz.poeschl.roborush.gamelogic.actions.RefuelAction
import xyz.poeschl.roborush.gamelogic.internal.MapHandler
import xyz.poeschl.roborush.gamelogic.internal.RobotHandler
import xyz.poeschl.roborush.models.Direction
import xyz.poeschl.roborush.models.TileType
import xyz.poeschl.roborush.models.settings.SettingKey
import xyz.poeschl.roborush.repositories.Map
import xyz.poeschl.roborush.repositories.RobotRepository
import xyz.poeschl.roborush.service.ConfigService
import xyz.poeschl.roborush.service.MapService
import xyz.poeschl.roborush.service.PlayedGamesService
import xyz.poeschl.roborush.test.utils.builder.Builders.Companion.a
import xyz.poeschl.roborush.test.utils.builder.Builders.Companion.listWithOne
import xyz.poeschl.roborush.test.utils.builder.ConfigTypes.Companion.`$IntSetting`
import xyz.poeschl.roborush.test.utils.builder.GameLogicBuilder.Companion.`$Map`
import xyz.poeschl.roborush.test.utils.builder.GameLogicBuilder.Companion.`$Position`
import xyz.poeschl.roborush.test.utils.builder.GameLogicBuilder.Companion.`$Robot`
import xyz.poeschl.roborush.test.utils.builder.GameLogicBuilder.Companion.`$Size`
import xyz.poeschl.roborush.test.utils.builder.GameLogicBuilder.Companion.`$Tile`
import java.util.*

class GamePlayTest {

  private val websocketController = mockk<WebsocketController>(relaxUnitFun = true)
  private val robotRepository = mockk<RobotRepository>()
  private val configService = mockk<ConfigService>()
  private val playedGamesService = mockk<PlayedGamesService>()
  private val mapService = mockk<MapService>()

  private val gameStateMachine = GameStateMachine(websocketController)
  private val mapHandler = MapHandler()
  private val robotHandler = RobotHandler(robotRepository, gameStateMachine)

  private val gameHandler = GameHandler(mapHandler, robotHandler, websocketController, gameStateMachine, configService, playedGamesService, mapService)

  private val robot = a(`$Robot`().withId(1))

  @Test
  fun refuelRobot() {
    // WHEN

    every { mapService.getNextChallengeMap() } returns createMapWithFuelStation()
    every { robotRepository.findById(robot.id!!) } returns Optional.of(robot)
    every { configService.getIntSetting(SettingKey.DISTANCE_ROBOT_SIGHT_ON_MOVE) } returns a(`$IntSetting`().withValue(1))

    gameStateMachine.setGameState(GameState.PREPARE)
    gameHandler.prepareNewGame()
    gameStateMachine.setGameState(GameState.WAIT_FOR_PLAYERS)
    gameHandler.registerRobotForNextGame(robot.id!!)

    // Position robot on fuel station tile
    gameStateMachine.setGameState(GameState.WAIT_FOR_ACTION)
    gameHandler.nextActionForRobot(robot.id!!, MoveAction(Direction.EAST))
    gameStateMachine.setGameState(GameState.ACTION)
    gameHandler.executeAllRobotActions()
    val beforeFuelRobot = gameHandler.getActiveRobot(robot.id!!)!!

    gameStateMachine.setGameState(GameState.WAIT_FOR_ACTION)

    // THEN
    gameHandler.nextActionForRobot(robot.id!!, RefuelAction())
    gameStateMachine.setGameState(GameState.ACTION)
    gameHandler.executeAllRobotActions()

    // VERIFY
    assertThat(mapHandler.getTileAtPosition(beforeFuelRobot.position).type).isEqualTo(TileType.FUEL_TILE)
    assertThat(beforeFuelRobot.fuel).isEqualTo(100 - 1)

    val currentRobot = gameHandler.getActiveRobot(robot.id!!)!!
    assertThat(currentRobot.fuel).isEqualTo(200)
  }

  private fun createMapWithFuelStation(): Map {
    val mapWithFuelStation = a(
      `$Map`()
        .withSize(a(`$Size`().withHeight(2).withWidth(2)))
        .withMaxRobotFuel(200)
        .withPossibleStartPositions(listWithOne(`$Position`().withX(0).withY(0)))
        .withTargetPosition(a(`$Position`().withX(1).withY(1)))
    )
    mapWithFuelStation.addTile(
      a(
        `$Tile`()
          .withPosition(a(`$Position`().withX(0).withY(0)))
          .withType(TileType.START_TILE)
          .withHeight(0)
      )
    )
    mapWithFuelStation.addTile(
      a(
        `$Tile`()
          .withPosition(a(`$Position`().withX(1).withY(0)))
          .withType(TileType.FUEL_TILE)
          .withHeight(100)
      )
    )
    mapWithFuelStation.addTile(
      a(
        `$Tile`()
          .withPosition(a(`$Position`().withX(0).withY(1)))
          .withType(TileType.DEFAULT_TILE)
          .withHeight(100)
      )
    )
    mapWithFuelStation.addTile(
      a(
        `$Tile`()
          .withPosition(a(`$Position`().withX(1).withY(1)))
          .withType(TileType.TARGET_TILE)
          .withHeight(100)
      )
    )
    return mapWithFuelStation
  }
}
