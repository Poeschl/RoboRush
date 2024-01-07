package xyz.poeschl.pathseeker.service

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import xyz.poeschl.pathseeker.gamelogic.GameHandler
import xyz.poeschl.pathseeker.models.Position
import xyz.poeschl.pathseeker.models.Tile

class MapServiceTest {

  private val gameHandler = mockk<GameHandler>()

  private val mapService = MapService(gameHandler)

  @Test
  fun getHeightMap() {
    // WHEN
    val tiles = listOf(Tile(Position(0, 0)))
    every { gameHandler.getHeightMap() } returns tiles

    // THEN
    val result = mapService.getHeightMap()

    // VERIFY
    assertThat(result).containsExactlyElementsOf(tiles)
  }
}
