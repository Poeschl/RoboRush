package xyz.poeschl.pathseeker.test.utils.builder

import org.junit.jupiter.api.Disabled
import xyz.poeschl.pathseeker.configuration.Builder
import xyz.poeschl.pathseeker.models.ActiveRobotBuilder
import xyz.poeschl.pathseeker.models.ColorBuilder
import xyz.poeschl.pathseeker.models.Direction
import xyz.poeschl.pathseeker.models.MapBuilder
import xyz.poeschl.pathseeker.models.PositionBuilder
import xyz.poeschl.pathseeker.models.PublicRobotBuilder
import xyz.poeschl.pathseeker.models.SizeBuilder
import xyz.poeschl.pathseeker.models.TileBuilder
import xyz.poeschl.pathseeker.repositories.RobotBuilder
import xyz.poeschl.pathseeker.test.utils.builder.Builders.Companion.a
import xyz.poeschl.pathseeker.test.utils.builder.Builders.Companion.listWithOne
import xyz.poeschl.pathseeker.test.utils.builder.NativeTypes.Companion.`$Id`
import xyz.poeschl.pathseeker.test.utils.builder.NativeTypes.Companion.`$Int`
import xyz.poeschl.pathseeker.test.utils.builder.SecurityBuilder.Companion.`$User`

@Disabled("Marking this file as a test class, to allow a relaxed linting")
class GameLogicBuilder {
  companion object {
    fun `$Robot`() = RobotBuilder()
      .withId(null)
      .withColor(a(`$Color`()))
      .withUser(a(`$User`()))

    fun `$ActiveRobot`() = ActiveRobotBuilder()
      .withId(a(`$Id`()))
      .withColor(a(`$Color`()))
      .withFuel(Int.MAX_VALUE)
      .withPosition(a(`$Position`()))

    fun `$PublicRobot`() = PublicRobotBuilder()
      .withId(a(`$Id`()))
      .withColor(a(`$Color`()))
      .withPosition(a(`$Position`()))

    fun `$Color`() = ColorBuilder()
      .withR(a(`$Int`(0, 255)))
      .withG(a(`$Int`(0, 255)))
      .withB(a(`$Int`(0, 255)))

    fun `$Map`() = MapBuilder()
      .withSize(a(`$Size`()))
      .withPossibleStartPositions(listWithOne(`$Position`()))
      .withMapData(arrayOf(arrayOf(a(`$Tile`()))))

    fun `$Tile`() = TileBuilder()
      .withPosition(a(`$Position`()))
      .withHeight(a(`$Int`()))

    fun `$Size`() = SizeBuilder()
      .withHeight(a(`$Int`()))
      .withWidth(a(`$Int`()))

    fun `$Position`() = PositionBuilder()
      .withX(a(`$Int`(0, Int.MAX_VALUE)))
      .withY(a(`$Int`(0, Int.MAX_VALUE)))

    fun `$Direction`() = Builder { Direction.entries.random() }
  }
}