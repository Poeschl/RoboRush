package xyz.poeschl.roborush.models.settings

abstract class Setting<T>(val key: SettingKey, val value: T) {
  abstract val type: SettingType

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Setting<*>

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

enum class SettingKey {
  TIMEOUT_WAIT_FOR_PLAYERS,
  TIMEOUT_WAIT_FOR_ACTION,
  TIMEOUT_GAME_END,
  TIMEOUT_VICTORY_SCREEN,
  THRESHOLD_NO_ROBOT_ACTION_END_GAME,
  TARGET_POSITION_IN_GAMEINFO
}

enum class SettingType {
  DURATION,
  INT,
  BOOLEAN
}
