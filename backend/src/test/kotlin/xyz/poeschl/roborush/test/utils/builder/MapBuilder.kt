package xyz.poeschl.roborush.test.utils.builder

import org.junit.jupiter.api.Disabled
import xyz.poeschl.roborush.controller.restmodels.MapAttributeSaveDtoBuilder
import xyz.poeschl.roborush.test.utils.builder.Builders.Companion.a
import xyz.poeschl.roborush.test.utils.builder.NativeTypes.Companion.`$Double`
import xyz.poeschl.roborush.test.utils.builder.NativeTypes.Companion.`$Int`
import xyz.poeschl.roborush.test.utils.builder.NativeTypes.Companion.`$String`

@Disabled("Marking this file as a test class, to allow a relaxed linting")
class MapBuilder {
  companion object {
    fun `$MapAttributeSaveDto`() = MapAttributeSaveDtoBuilder()
      .withMapName(a(`$String`()))
      .withMaxRobotFuel(a(`$Int`()))
      .withSolarChargeRate(a(`$Double`()))
  }
}
