package xyz.poeschl.roborush.test.utils.builder

import org.junit.jupiter.api.Disabled
import xyz.poeschl.roborush.models.settings.SettingKey
import xyz.poeschl.roborush.models.settings.SettingType
import xyz.poeschl.roborush.repositories.SettingEntityBuilder
import xyz.poeschl.roborush.test.utils.builder.Builders.Companion.a
import xyz.poeschl.roborush.test.utils.builder.NativeTypes.Companion.`$Id`
import xyz.poeschl.roborush.test.utils.builder.NativeTypes.Companion.`$String`

@Disabled("Marking this file as a test class, to allow a relaxed linting")
class ConfigTypes {
  companion object {
    fun `$SettingEntity`() = SettingEntityBuilder()
      .withId(a(`$Id`()))
      .withKey(SettingKey.entries.random())
      .withType(SettingType.entries.random())
      .withValue(a(`$String`()))
  }
}
