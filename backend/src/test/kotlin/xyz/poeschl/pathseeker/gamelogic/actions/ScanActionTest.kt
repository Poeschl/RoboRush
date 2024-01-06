package xyz.poeschl.pathseeker.gamelogic.actions

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import xyz.poeschl.pathseeker.exceptions.InsufficientFuelException
import xyz.poeschl.pathseeker.gamelogic.GameHandler
import xyz.poeschl.pathseeker.models.ActiveRobot
import xyz.poeschl.pathseeker.models.Color
import xyz.poeschl.pathseeker.models.Position
import xyz.poeschl.pathseeker.models.Tile

class ScanActionTest {

  private val gameHandler = mock<GameHandler>()

  @Test
  fun scanCheck() {
    // WHEN
    val robot = ActiveRobot(1, Color(1, 2, 3), 100, Position(1, 1))
    val scannedTiles = listOf(Tile(Position(1, 0), 1), Tile(Position(0, 1), 1))
    val cost = 10

    `when`(gameHandler.getTilesInDistance(Position(1, 1), 2)).thenReturn(Pair(scannedTiles, cost))
    val action = ScanAction(2)

    // THEN
    action.check(robot, gameHandler)

    // VERIFY by no exception
  }

  @Test
  fun scanCheck_withoutFuel() {
    // WHEN
    val robot = ActiveRobot(1, Color(1, 2, 3), 100, Position(1, 1))
    val scannedTiles = listOf(Tile(Position(1, 0), 1), Tile(Position(0, 1), 1))
    val cost = 101

    `when`(gameHandler.getTilesInDistance(Position(1, 1), 2)).thenReturn(Pair(scannedTiles, cost))
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
    val robot = ActiveRobot(1, Color(1, 2, 3), 100, Position(1, 1))
    val scannedTiles = listOf(Tile(Position(1, 0), 1), Tile(Position(0, 1), 1))
    val cost = 10

    `when`(gameHandler.getTilesInDistance(Position(1, 1), 2)).thenReturn(Pair(scannedTiles, cost))
    val action = ScanAction(2)

    // THEN
    val tiles = action.action(robot, gameHandler)

    // VERIFY
    assertThat(tiles).containsAll(scannedTiles)
    assertThat(robot.fuel).isEqualTo(90)
  }
}
