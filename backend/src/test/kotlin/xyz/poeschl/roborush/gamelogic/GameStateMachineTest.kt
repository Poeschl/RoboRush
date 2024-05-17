package xyz.poeschl.roborush.gamelogic

import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import xyz.poeschl.roborush.controller.WebsocketController
import xyz.poeschl.roborush.exceptions.GameStateException
import java.util.stream.Stream

class GameStateMachineTest {

  companion object {
    @JvmStatic
    fun stateTestArgs(): Stream<Arguments> = Stream.of(
      Arguments.of(listOf<GameState>(), GameState.PREPARE, true),
      Arguments.of(listOf<GameState>(), GameState.WAIT_FOR_PLAYERS, false),
      Arguments.of(listOf<GameState>(), GameState.WAIT_FOR_ACTION, false),
      Arguments.of(listOf<GameState>(), GameState.ACTION, false),
      Arguments.of(listOf<GameState>(), GameState.ENDED, false),
      Arguments.of(listOf(GameState.PREPARE), GameState.WAIT_FOR_PLAYERS, true),
      Arguments.of(listOf(GameState.PREPARE), GameState.WAIT_FOR_ACTION, false),
      Arguments.of(listOf(GameState.PREPARE), GameState.ACTION, false),
      Arguments.of(listOf(GameState.PREPARE), GameState.ENDED, false),
      Arguments.of(listOf(GameState.PREPARE, GameState.WAIT_FOR_PLAYERS), GameState.WAIT_FOR_ACTION, true),
      Arguments.of(listOf(GameState.PREPARE, GameState.WAIT_FOR_PLAYERS), GameState.ACTION, false),
      Arguments.of(listOf(GameState.PREPARE, GameState.WAIT_FOR_PLAYERS), GameState.ENDED, false),
      Arguments.of(listOf(GameState.PREPARE, GameState.WAIT_FOR_PLAYERS), GameState.PREPARE, false),
      Arguments.of(listOf(GameState.PREPARE, GameState.WAIT_FOR_PLAYERS, GameState.WAIT_FOR_ACTION), GameState.WAIT_FOR_PLAYERS, false),
      Arguments.of(listOf(GameState.PREPARE, GameState.WAIT_FOR_PLAYERS, GameState.WAIT_FOR_ACTION), GameState.ACTION, true),
      Arguments.of(listOf(GameState.PREPARE, GameState.WAIT_FOR_PLAYERS, GameState.WAIT_FOR_ACTION), GameState.ENDED, false),
      Arguments.of(listOf(GameState.PREPARE, GameState.WAIT_FOR_PLAYERS, GameState.WAIT_FOR_ACTION), GameState.PREPARE, false),
      Arguments.of(listOf(GameState.PREPARE, GameState.WAIT_FOR_PLAYERS, GameState.WAIT_FOR_ACTION, GameState.ACTION), GameState.WAIT_FOR_ACTION, true),
      Arguments.of(listOf(GameState.PREPARE, GameState.WAIT_FOR_PLAYERS, GameState.WAIT_FOR_ACTION, GameState.ACTION), GameState.WAIT_FOR_PLAYERS, false),
      Arguments.of(listOf(GameState.PREPARE, GameState.WAIT_FOR_PLAYERS, GameState.WAIT_FOR_ACTION, GameState.ACTION), GameState.ENDED, true),
      Arguments.of(listOf(GameState.PREPARE, GameState.WAIT_FOR_PLAYERS, GameState.WAIT_FOR_ACTION, GameState.ACTION), GameState.ACTION, false),
      Arguments.of(
        listOf(GameState.PREPARE, GameState.WAIT_FOR_PLAYERS, GameState.WAIT_FOR_ACTION, GameState.ACTION, GameState.WAIT_FOR_ACTION),
        GameState.ACTION,
        true
      ),
      Arguments.of(
        listOf(GameState.PREPARE, GameState.WAIT_FOR_PLAYERS, GameState.WAIT_FOR_ACTION, GameState.ACTION, GameState.WAIT_FOR_ACTION),
        GameState.ENDED,
        false
      ),
      Arguments.of(
        listOf(GameState.PREPARE, GameState.WAIT_FOR_PLAYERS, GameState.WAIT_FOR_ACTION, GameState.ACTION, GameState.WAIT_FOR_ACTION),
        GameState.PREPARE,
        false
      ),
      Arguments.of(listOf(GameState.PREPARE, GameState.WAIT_FOR_PLAYERS, GameState.WAIT_FOR_ACTION, GameState.ACTION, GameState.ENDED), GameState.PREPARE, true),
      Arguments.of(listOf(GameState.PREPARE, GameState.WAIT_FOR_PLAYERS, GameState.WAIT_FOR_ACTION, GameState.ACTION, GameState.ENDED), GameState.WAIT_FOR_PLAYERS, false),
      Arguments.of(listOf(GameState.PREPARE, GameState.WAIT_FOR_PLAYERS, GameState.WAIT_FOR_ACTION, GameState.ACTION, GameState.ENDED), GameState.WAIT_FOR_ACTION, false),
      Arguments.of(listOf(GameState.PREPARE, GameState.WAIT_FOR_PLAYERS, GameState.WAIT_FOR_ACTION, GameState.ACTION, GameState.ENDED), GameState.ACTION, false)
    )
  }

  private val websocketController = mockk<WebsocketController>(relaxUnitFun = true)

  private val gameStateService = GameStateMachine(websocketController)

  @ParameterizedTest
  @MethodSource("stateTestArgs")
  fun setGameState(preparation: List<GameState>, testState: GameState, successFull: Boolean) {
    // WHEN
    preparation.forEach { gameStateService.setGameState(it) }

    if (successFull) {
      // THEN be successful
      gameStateService.setGameState(testState)

      verify { websocketController.sendGameStateUpdate(testState) }
    } else {
      // VERIFY
      assertThrows<GameStateException> {
        gameStateService.setGameState(testState)
      }
      assertThat(!gameStateService.isInState(testState))
    }
  }

  @Test
  fun isInState_initial() {
    // WHEN
    val initState = GameState.ENDED

    // THEN
    val result = gameStateService.isInState(initState)

    // VERIFY
    assertThat(result).isTrue()
  }

  @Test
  fun isInState() {
    // WHEN
    val state = GameState.PREPARE
    gameStateService.setGameState(GameState.PREPARE)

    // THEN
    val result = gameStateService.isInState(state)

    // VERIFY
    assertThat(result).isTrue()
  }

  @Test
  fun isInState_wrong() {
    // WHEN
    val state = GameState.ENDED
    gameStateService.setGameState(GameState.PREPARE)

    // THEN
    val result = gameStateService.isInState(state)

    // VERIFY
    assertThat(result).isFalse()
  }

  @Test
  fun getCurrentState() {
    // WHEN

    // THEN
    val result = gameStateService.getCurrentState()

    // VERIFY
    assertThat(result).isEqualTo(GameState.ENDED)
  }
}
