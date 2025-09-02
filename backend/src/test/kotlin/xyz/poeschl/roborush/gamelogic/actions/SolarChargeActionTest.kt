package xyz.poeschl.roborush.gamelogic.actions

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import xyz.poeschl.roborush.exceptions.ActionDeniedByConfig
import xyz.poeschl.roborush.exceptions.TankFullException
import xyz.poeschl.roborush.gamelogic.GameHandler
import xyz.poeschl.roborush.gamelogic.actions.RefuelAction.RefuelActionResult
import xyz.poeschl.roborush.test.utils.builder.Builders.Companion.a
import xyz.poeschl.roborush.test.utils.builder.GameLogicBuilder.Companion.`$ActiveRobot`

class SolarChargeActionTest {

  private val gameHandler = mockk<GameHandler>(relaxUnitFun = true)

  @Test
  fun chargeCheck() {
    // WHEN
    val robot = a(`$ActiveRobot`().withFuel(100).withMaxFuel(100))
    // Simulate tank depletion
    robot.fuel = 10
    val action = SolarChargeAction()

    every { gameHandler.isSolarChargePossible() } returns true

    // THEN
    action.check(robot, gameHandler)

    // VERIFY by no exception
  }

  @Test
  fun chargeCheck_mapDenial() {
    // WHEN
    val robot = a(`$ActiveRobot`().withFuel(10).withMaxFuel(100))
    val action = SolarChargeAction()

    every { gameHandler.isSolarChargePossible() } returns false

    // THEN
    assertThrows<ActionDeniedByConfig> {
      action.check(robot, gameHandler)
    }

    // VERIFY by no exception
  }

  @Test
  fun chargeCheck_tankFull() {
    // WHEN
    val robot = a(`$ActiveRobot`().withFuel(10).withMaxFuel(10))
    val action = SolarChargeAction()

    every { gameHandler.isSolarChargePossible() } returns true

    // THEN
    assertThrows<TankFullException> {
      action.check(robot, gameHandler)
    }

    // VERIFY by no exception
  }

  @Test
  fun chargeAction() {
    // WHEN
    val maxRobotFuel = 100
    val chargeRate = .5
    val robot = a(`$ActiveRobot`().withFuel(maxRobotFuel).withMaxFuel(maxRobotFuel))
    // After robot workout, less energy is left
    robot.fuel = 10
    val action = SolarChargeAction()

    every { gameHandler.getRobotMaxFuel() } returns maxRobotFuel
    every { gameHandler.getSolarChargeRate() } returns chargeRate

    // THEN
    val actionResult = action.action(robot, gameHandler)

    // VERIFY
    val expectedFuel = (10 + chargeRate * maxRobotFuel).toInt()
    assertThat(actionResult.updatedRobot.fuel).isEqualTo(expectedFuel)
    assertThat(actionResult.result).isEqualTo(RefuelActionResult(60))
  }

  @Test
  fun chargeAction_maxLimit() {
    // WHEN
    val maxRobotFuel = 100
    val chargeRate = .5
    val robot = a(`$ActiveRobot`().withFuel(maxRobotFuel).withMaxFuel(maxRobotFuel))
    // After a light robot workout, only a little is missing
    robot.fuel = 99
    val action = SolarChargeAction()

    every { gameHandler.getRobotMaxFuel() } returns maxRobotFuel
    every { gameHandler.getSolarChargeRate() } returns chargeRate

    // THEN
    val actionResult = action.action(robot, gameHandler)

    // VERIFY
    assertThat(actionResult.updatedRobot.fuel).isEqualTo(maxRobotFuel)
    assertThat(actionResult.result).isEqualTo(RefuelActionResult(100))
  }
}
