package xyz.poeschl.roborush.test.utils.builder

import org.junit.jupiter.api.Disabled
import xyz.poeschl.roborush.configuration.Builder
import xyz.poeschl.roborush.models.settings.*
import xyz.poeschl.roborush.repositories.SettingEntityBuilder
import xyz.poeschl.roborush.test.utils.builder.Builders.Companion.a
import xyz.poeschl.roborush.test.utils.builder.NativeTypes.Companion.`$Boolean`
import xyz.poeschl.roborush.test.utils.builder.NativeTypes.Companion.`$Id`
import xyz.poeschl.roborush.test.utils.builder.NativeTypes.Companion.`$Int`
import xyz.poeschl.roborush.test.utils.builder.NativeTypes.Companion.`$String`
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Disabled("Marking this file as a test class, to allow a relaxed linting")
class ConfigTypes {
  companion object {
    fun `$SettingEntity`() = SettingEntityBuilder()
      .withId(a(`$Id`()))
      .withKey(SettingKey.entries.random())
      .withType(SettingType.entries.random())
      .withValue(a(`$String`()))

    fun `$BooleanSetting`() = BooleanSettingBuilder()
      .withKey(SettingKey.entries.random())
      .withValue(a(`$Boolean`()))

    fun `$DurationSetting`() = Builder { DurationSetting(SettingKey.entries.random(), Duration.ZERO.plus(Random.nextInt(1000).seconds)) }

    fun `$IntSetting`() = IntSettingBuilder()
      .withKey(SettingKey.entries.random())
      .withValue(a(`$Int`()))
  }
}
