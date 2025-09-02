package xyz.poeschl.roborush.models.settings

import kotlin.time.Duration

class DurationSetting(key: SettingKey, value: Duration) : Setting<Duration>(key, value) {
  override val type: SettingType
    get() = SettingType.DURATION

  fun inWholeMilliseconds() = value.inWholeMilliseconds
  override fun toString(): String = "DurationSetting(key=$key, value=$value)"
}
