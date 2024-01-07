package xyz.poeschl.pathseeker.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import xyz.poeschl.pathseeker.gamelogic.GameHandler
import xyz.poeschl.pathseeker.models.Position
import xyz.poeschl.pathseeker.models.Tile

class MapServiceTest {

  private val gameHandler = mock<GameHandler>()

  private val mapService = MapService(gameHandler)

  @Test
  fun getHeightMap() {
    // WHEN
    val tiles = listOf(Tile(Position(0, 0)))
    `when`(gameHandler.getHeightMap()).thenReturn(tiles)

    // THEN
    val result = mapService.getHeightMap()

    // VERIFY
    assertThat(result).containsExactlyElementsOf(tiles)
  }
}
