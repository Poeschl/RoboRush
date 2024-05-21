package xyz.poeschl.roborush.test.utils.builder

import org.junit.jupiter.api.Disabled
import xyz.poeschl.roborush.configuration.Builder
import xyz.poeschl.roborush.gamelogic.GameState
import xyz.poeschl.roborush.models.*
import xyz.poeschl.roborush.repositories.MapBuilder
import xyz.poeschl.roborush.repositories.RobotBuilder
import xyz.poeschl.roborush.repositories.TileBuilder
import xyz.poeschl.roborush.test.utils.builder.Builders.Companion.a
import xyz.poeschl.roborush.test.utils.builder.Builders.Companion.listWithOne
import xyz.poeschl.roborush.test.utils.builder.NativeTypes.Companion.`$Id`
import xyz.poeschl.roborush.test.utils.builder.NativeTypes.Companion.`$Int`
import xyz.poeschl.roborush.test.utils.builder.NativeTypes.Companion.`$String`
import xyz.poeschl.roborush.test.utils.builder.SecurityBuilder.Companion.`$User`

@Disabled("Marking this file as a test class, to allow a relaxed linting")
class GameLogicBuilder {
  companion object {
    fun `$Robot`() = RobotBuilder()
      .withId(null)
      .withColor(a(`$Color`()))
      .withUser(a(`$User`()))

    fun `$ActiveRobot`() = ActiveRobotBuilder()
      .withId(a(`$Id`()))
      .withName(a(`$String`("user")))
      .withUser(a(`$User`()))
      .withColor(a(`$Color`()))
      .withFuel(Int.MAX_VALUE)
      .withPosition(a(`$Position`()))
      .withKnownPositions(mutableSetOf())

    fun `$PublicRobot`() = PublicRobotBuilder()
      .withId(a(`$Id`()))
      .withName(a(`$String`("user")))
      .withColor(a(`$Color`()))
      .withPosition(a(`$Position`()))

    fun `$Color`() = ColorBuilder()
      .withR(a(`$Int`(0, 255)))
      .withG(a(`$Int`(0, 255)))
      .withB(a(`$Int`(0, 255)))

    fun `$Map`() = MapBuilder()
      .withId(null)
      .withMapName(a(`$String`()))
      .withSize(a(`$Size`()))
      .withPossibleStartPositions(listWithOne(`$Position`()))
      .withTargetPosition(a(`$Position`()))
      .withPossibleStartPositions(listWithOne(`$Position`()))

    fun `$Tile`() = TileBuilder()
      .withId(null)
      .withPosition(a(`$Position`()))
      .withHeight(a(`$Int`()))
      .withType(TileType.DEFAULT_TILE)

    fun `$Size`() = SizeBuilder()
      .withHeight(a(`$Int`()))
      .withWidth(a(`$Int`()))

    fun `$Position`() = PositionBuilder()
      .withX(a(`$Int`(0, Int.MAX_VALUE)))
      .withY(a(`$Int`(0, Int.MAX_VALUE)))

    fun `$Direction`() = Builder { Direction.entries.random() }

    fun `$GameState`() = Builder { GameState.entries.random() }
  }
}
