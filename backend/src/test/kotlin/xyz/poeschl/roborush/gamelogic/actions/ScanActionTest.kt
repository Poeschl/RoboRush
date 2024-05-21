package xyz.poeschl.roborush.gamelogic.actions

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import xyz.poeschl.roborush.exceptions.InsufficientFuelException
import xyz.poeschl.roborush.gamelogic.GameHandler
import xyz.poeschl.roborush.models.Position
import xyz.poeschl.roborush.test.utils.builder.Builders.Companion.a
import xyz.poeschl.roborush.test.utils.builder.GameLogicBuilder.Companion.`$ActiveRobot`
import xyz.poeschl.roborush.test.utils.builder.GameLogicBuilder.Companion.`$Tile`

class ScanActionTest {

  private val gameHandler = mockk<GameHandler>()
  private val objectMapper = jacksonObjectMapper()

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
    assertThat(robot.knownPositions).containsAll(scannedTiles.map { it.position })
  }

  @Test
  fun checkJsonForScan() {
    // WHEN
    val move = ScanAction(100)

    // THEN
    val json = objectMapper.writeValueAsString(move)

    // VERIFY
    assertThat(json).isEqualTo("""{"type":"scan","distance":100}""")
  }
}
