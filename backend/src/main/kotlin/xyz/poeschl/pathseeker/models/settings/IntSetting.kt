package xyz.poeschl.pathseeker.models.settings

class IntSetting(key: SettingKey, value: Int) : Setting<Int>(key, value) {
  override val type: SettingType
    get() = SettingType.INT

  override fun toString(): String {
    return "IntSetting(key=$key, value=$value)"
  }
}
