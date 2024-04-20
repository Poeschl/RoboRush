package xyz.poeschl.pathseeker.gamelogic.actions

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import xyz.poeschl.pathseeker.gamelogic.GameHandler
import xyz.poeschl.pathseeker.test.utils.builder.Builders.Companion.a
import xyz.poeschl.pathseeker.test.utils.builder.GameLogicBuilder.Companion.`$ActiveRobot`
import xyz.poeschl.pathseeker.test.utils.builder.GameLogicBuilder.Companion.`$Position`

class WaitActionTest {

  private val gameHandler = mockk<GameHandler>(relaxUnitFun = true)
  private val objectMapper = jacksonObjectMapper()

  @Test
  fun action() {
    // WHEN
    val position = a(`$Position`())
    val robot = a(`$ActiveRobot`().withPosition(position))
    val waitAction = WaitAction()

    // THEN
    val result = waitAction.action(robot, gameHandler)

    // VERIFY
    assertThat(result).isEqualTo(position)
  }
}
