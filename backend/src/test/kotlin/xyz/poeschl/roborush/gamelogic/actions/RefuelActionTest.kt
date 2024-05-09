package xyz.poeschl.roborush.gamelogic.actions

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import xyz.poeschl.roborush.exceptions.WrongTileTypeException
import xyz.poeschl.roborush.gamelogic.GameHandler
import xyz.poeschl.roborush.models.Position
import xyz.poeschl.roborush.models.TileType
import xyz.poeschl.roborush.test.utils.builder.Builders.Companion.a
import xyz.poeschl.roborush.test.utils.builder.GameLogicBuilder.Companion.`$ActiveRobot`
import xyz.poeschl.roborush.test.utils.builder.GameLogicBuilder.Companion.`$Tile`

class RefuelActionTest {

  private val gameHandler = mockk<GameHandler>()

  @Test
  fun refuelCheck() {
    // WHEN
    val robot = a(`$ActiveRobot`().withPosition(Position(1, 1)))
    val action = RefuelAction()

    every { gameHandler.getTileAtPosition(Position(1, 1)) } returns a(`$Tile`().withType(TileType.FUEL_TILE))

    // THEN
    action.check(robot, gameHandler)

    // VERIFY by no exception
  }

  @Test
  fun refuelCheck_notOnFuelTile() {
    // WHEN
    val robot = a(`$ActiveRobot`().withPosition(Position(1, 1)))
    val action = RefuelAction()

    every { gameHandler.getTileAtPosition(Position(1, 1)) } returns a(`$Tile`().withType(TileType.DEFAULT_TILE))

    // THEN
    assertThrows<WrongTileTypeException> {
      action.check(robot, gameHandler)
    }

    // VERIFY
  }

  @Test
  fun refuelAction() {
    // WHEN
    val robot = a(`$ActiveRobot`().withFuel(10))
    val maxRobotFuel = 300
    val action = RefuelAction()

    every { gameHandler.getRobotMaxFuel() } returns maxRobotFuel

    // THEN
    val currentFuel = action.action(robot, gameHandler)

    // VERIFY
    assertThat(robot.fuel).isEqualTo(maxRobotFuel)
    assertThat(currentFuel).isEqualTo(maxRobotFuel)
  }
}
