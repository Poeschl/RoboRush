package xyz.poeschl.roborush.gamelogic.actions

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import xyz.poeschl.roborush.exceptions.ActionDeniedByConfig
import xyz.poeschl.roborush.gamelogic.GameHandler
import xyz.poeschl.roborush.test.utils.builder.Builders.Companion.a
import xyz.poeschl.roborush.test.utils.builder.GameLogicBuilder.Companion.`$ActiveRobot`

class SolarChargeActionTest {

  private val gameHandler = mockk<GameHandler>()

  @Test
  fun chargeCheck() {
    // WHEN
    val robot = a(`$ActiveRobot`().withFuel(10))
    val action = SolarChargeAction()

    every { gameHandler.isSolarChargePossible() } returns true

    // THEN
    action.check(robot, gameHandler)

    // VERIFY by no exception
  }

  @Test
  fun chargeCheck_mapDenial() {
    // WHEN
    val robot = a(`$ActiveRobot`().withFuel(10))
    val action = SolarChargeAction()

    every { gameHandler.isSolarChargePossible() } returns false

    // THEN
    assertThrows<ActionDeniedByConfig> {
      action.check(robot, gameHandler)
    }

    // VERIFY by no exception
  }

  @Test
  fun chargeAction() {
    // WHEN
    val maxRobotFuel = 100
    val chargeRate = .5
    val robot = a(`$ActiveRobot`().withFuel(maxRobotFuel))
    // After robot workout, less energy is left
    robot.fuel = 10
    val action = SolarChargeAction()

    every { gameHandler.getRobotMaxFuel() } returns maxRobotFuel
    every { gameHandler.getSolarChargeRate() } returns chargeRate

    // THEN
    val currentFuel = action.action(robot, gameHandler)

    // VERIFY
    val expectedFuel = (10 + chargeRate * maxRobotFuel).toInt()
    assertThat(robot.fuel).isEqualTo(expectedFuel)
    assertThat(currentFuel).isEqualTo(expectedFuel)
  }

  @Test
  fun chargeAction_maxLimit() {
    // WHEN
    val maxRobotFuel = 100
    val chargeRate = .5
    val robot = a(`$ActiveRobot`().withFuel(maxRobotFuel))
    // After a light robot workout, only a little is missing
    robot.fuel = 99
    val action = SolarChargeAction()

    every { gameHandler.getRobotMaxFuel() } returns maxRobotFuel
    every { gameHandler.getSolarChargeRate() } returns chargeRate

    // THEN
    val currentFuel = action.action(robot, gameHandler)

    // VERIFY
    assertThat(robot.fuel).isEqualTo(maxRobotFuel)
    assertThat(currentFuel).isEqualTo(maxRobotFuel)
  }
}
