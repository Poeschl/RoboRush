package xyz.poeschl.pathseeker.test.utils.builder

import org.junit.jupiter.api.Disabled
import xyz.poeschl.pathseeker.models.SettingKey
import xyz.poeschl.pathseeker.models.SettingType
import xyz.poeschl.pathseeker.repositories.SettingEntityBuilder
import xyz.poeschl.pathseeker.test.utils.builder.Builders.Companion.a
import xyz.poeschl.pathseeker.test.utils.builder.NativeTypes.Companion.`$Id`
import xyz.poeschl.pathseeker.test.utils.builder.NativeTypes.Companion.`$String`

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
