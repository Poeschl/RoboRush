package xyz.poeschl.pathseeker.gamelogic.actions

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import xyz.poeschl.pathseeker.exceptions.InsufficientFuelException
import xyz.poeschl.pathseeker.gamelogic.GameHandler
import xyz.poeschl.pathseeker.models.Position
import xyz.poeschl.pathseeker.test.utils.builder.Builders.Companion.a
import xyz.poeschl.pathseeker.test.utils.builder.GameLogicBuilder.Companion.`$ActiveRobot`
import xyz.poeschl.pathseeker.test.utils.builder.GameLogicBuilder.Companion.`$Tile`

class ScanActionTest {

  private val gameHandler = mockk<GameHandler>()

  @Test
  fun scanCheck() {
    // WHEN
    val robot = a(`$ActiveRobot`().withPosition(Position(1, 1)))
    val scannedTiles = listOf(a(`$Tile`()), a(`$Tile`()))
    val cost = 10

    every { gameHandler.getTilesInDistance(Position(1, 1), 2) } returns Pair(scannedTiles, cost)
    val action = ScanAction(2)

    // THEN
    action.check(robot, gameHandler)

    // VERIFY by no exception
  }

  @Test
  fun scanCheck_withoutFuel() {
    // WHEN
    val robot = a(`$ActiveRobot`().withPosition(Position(1, 1)).withFuel(100))
    val scannedTiles = listOf(a(`$Tile`()), a(`$Tile`()))
    val cost = 101

    every { gameHandler.getTilesInDistance(Position(1, 1), 2) } returns Pair(scannedTiles, cost)
    val action = ScanAction(2)

    // THEN
    assertThrows<InsufficientFuelException> {
      action.check(robot, gameHandler)
    }

    // VERIFY
  }

  @Test
  fun scanAction() {
    // WHEN
    val robot = a(`$ActiveRobot`().withPosition(Position(1, 1)).withFuel(100))
    val scannedTiles = listOf(a(`$Tile`()), a(`$Tile`()))
    val cost = 10

    every { gameHandler.getTilesInDistance(Position(1, 1), 2) } returns Pair(scannedTiles, cost)
    val action = ScanAction(2)

    // THEN
    val tiles = action.action(robot, gameHandler)

    // VERIFY
    assertThat(tiles).containsAll(scannedTiles)
    assertThat(robot.fuel).isEqualTo(90)
  }
}
