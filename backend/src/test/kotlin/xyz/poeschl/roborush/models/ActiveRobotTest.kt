package xyz.poeschl.roborush.models

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import xyz.poeschl.roborush.security.repository.User
import xyz.poeschl.roborush.test.utils.builder.Builders.Companion.a
import xyz.poeschl.roborush.test.utils.builder.GameLogicBuilder.Companion.`$ActiveRobot`

class ActiveRobotTest {

  @Test
  fun maxFuel_shouldRemainConstant_afterCopy() {
    // GIVEN - Create a robot directly with 100 fuel and 100 maxFuel
    val user = User(1L, "test", "test")
    val color = Color(255, 0, 0)
    val position = Position(0, 0)
    val robot = ActiveRobot(1L, "testBot", user, color, 100, 100, position)

    // VERIFY initial state
    assertThat(robot.fuel).isEqualTo(100)
    assertThat(robot.maxFuel).isEqualTo(100)

    // WHEN - Robot uses some fuel
    robot.useFuel(30)
    assertThat(robot.fuel).isEqualTo(70)
    assertThat(robot.maxFuel).isEqualTo(100) // Still correct on original robot

    // WHEN - We copy the robot (like RobotHandler.getActiveRobot() does)
    val copiedRobot = robot.copy()

    // THEN - The copied robot should still have correct maxFuel (this was the bug)
    assertThat(copiedRobot.fuel).isEqualTo(70)
    assertThat(copiedRobot.maxFuel).isEqualTo(100) // Should now remain 100 after fix
  }

  @Test
  fun maxFuel_shouldRemainConstant_whenFuelChanges() {
    // GIVEN - Create a robot with maxFuel capacity
    val robot = a(`$ActiveRobot`().withFuel(100).withMaxFuel(100))

    // VERIFY initial state
    assertThat(robot.fuel).isEqualTo(100)
    assertThat(robot.maxFuel).isEqualTo(100)

    // WHEN - Robot uses some fuel
    robot.useFuel(30)

    // THEN - maxFuel should remain constant while fuel decreases
    assertThat(robot.fuel).isEqualTo(70)
    assertThat(robot.maxFuel).isEqualTo(100) // This should NOT change
  }

  @Test
  fun addFuel_shouldRespectMaxFuelCapacity() {
    // GIVEN - Create a robot with some fuel used
    val robot = a(`$ActiveRobot`().withFuel(100).withMaxFuel(100))
    robot.useFuel(50) // Now has 50/100 fuel

    // WHEN - Try to add more fuel than capacity allows
    robot.addFuel(80) // Should be capped at maxFuel

    // THEN - Fuel should be capped at maxFuel capacity
    assertThat(robot.fuel).isEqualTo(100) // Should be capped at maxFuel
    assertThat(robot.maxFuel).isEqualTo(100) // Should remain constant
  }

  @Test
  fun canRetrieveFuel_shouldCheckAgainstMaxFuel() {
    // GIVEN - Robot with full tank
    val robot = a(`$ActiveRobot`().withFuel(100).withMaxFuel(100))

    // WHEN - Tank is full
    // THEN - Should not be able to retrieve fuel
    assertThat(robot.canRetrieveFuel()).isFalse()

    // WHEN - Use some fuel
    robot.useFuel(20)

    // THEN - Should be able to retrieve fuel
    assertThat(robot.canRetrieveFuel()).isTrue()
    assertThat(robot.maxFuel).isEqualTo(100) // maxFuel should remain constant
  }
}
