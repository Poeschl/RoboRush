package xyz.poeschl.pathseeker.models

import org.springframework.stereotype.Component
import xyz.poeschl.pathseeker.repositories.SettingEntity
import kotlin.time.Duration

interface Setting<T> {
  val key: SettingKey
  val type: SettingType
  val value: T
}

class DurationSetting(override val key: SettingKey, override val value: Duration) : Setting<Duration> {
  override val type: SettingType
    get() = SettingType.DURATION
}

class IntSetting(override val key: SettingKey, override val value: Int) : Setting<Int> {
  override val type: SettingType
    get() = SettingType.INT
}

enum class SettingKey {
  TIMEOUT_WAIT_FOR_PLAYERS,
  TIMEOUT_WAIT_FOR_ACTION,
  TIMEOUT_GAME_END,
  THRESHOLD_NO_ROBOT_ACTION_END_GAME
}

enum class SettingType {
  DURATION,
  INT
}

@Component
class SettingEntityMapper {

  fun <T> toEntity(setting: Setting<T>, id: Long): SettingEntity {
    return when (setting.type) {
      SettingType.INT -> SettingEntity(id, setting.key, setting.type, (setting.value as Int).toString())
      SettingType.DURATION -> SettingEntity(id, setting.key, setting.type, (setting.value as Duration).toIsoString())
    }
  }

  fun fromEntity(settingEntity: SettingEntity): Setting<*> {
    return when (settingEntity.type) {
      SettingType.INT -> IntSetting(settingEntity.key, settingEntity.value.toInt())
      SettingType.DURATION -> DurationSetting(settingEntity.key, Duration.parseIsoString(settingEntity.value))
    }
  }
}
