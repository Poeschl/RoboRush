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

  fun inWholeMilliseconds() = value.inWholeMilliseconds
  override fun toString(): String {
    return "DurationSetting(key=$key, value=$value)"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as DurationSetting

    if (key != other.key) return false
    if (value != other.value) return false
    if (type != other.type) return false

    return true
  }

  override fun hashCode(): Int {
    var result = key.hashCode()
    result = 31 * result + value.hashCode()
    result = 31 * result + type.hashCode()
    return result
  }
}

class IntSetting(override val key: SettingKey, override val value: Int) : Setting<Int> {
  override val type: SettingType
    get() = SettingType.INT

  override fun toString(): String {
    return "IntSetting(key=$key, value=$value)"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as IntSetting

    if (key != other.key) return false
    if (value != other.value) return false
    if (type != other.type) return false

    return true
  }

  override fun hashCode(): Int {
    var result = key.hashCode()
    result = 31 * result + value
    result = 31 * result + type.hashCode()
    return result
  }
}

class SaveSettingDto(val key: SettingKey, val value: String)

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

  /**
   * Convert a settingInput to a SettingEntity. It also makes sure to get the right type.
   */
  fun toEntity(settingEntity: SettingEntity, settingInput: SaveSettingDto): SettingEntity {
    return when (settingEntity.type) {
      SettingType.INT -> SettingEntity(settingEntity.id, settingEntity.key, settingEntity.type, settingInput.value.toInt().toString())
      SettingType.DURATION -> SettingEntity(settingEntity.id, settingEntity.key, settingEntity.type, Duration.parseIsoString(settingInput.value).toIsoString())
    }
  }

  fun toEntity(setting: Setting<*>, id: Long): SettingEntity {
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
