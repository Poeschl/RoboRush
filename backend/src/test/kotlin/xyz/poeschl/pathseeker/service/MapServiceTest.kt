package xyz.poeschl.pathseeker.service

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import xyz.poeschl.pathseeker.gamelogic.GameHandler
import xyz.poeschl.pathseeker.test.utils.builder.Builders.Companion.listWithOne
import xyz.poeschl.pathseeker.test.utils.builder.GameLogicBuilder.Companion.`$Tile`

class MapServiceTest {

  private val gameHandler = mockk<GameHandler>()

  private val mapService = MapService(gameHandler)

  @Test
  fun getHeightMap() {
    // WHEN
    val tiles = listWithOne(`$Tile`())
    every { gameHandler.getHeightMap() } returns tiles

    // THEN
    val result = mapService.getHeightMap()

    // VERIFY
    assertThat(result).containsExactlyElementsOf(tiles)
  }
}
