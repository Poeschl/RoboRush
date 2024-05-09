package xyz.poeschl.roborush.models.settings

class BooleanSetting(key: SettingKey, value: Boolean) : Setting<Boolean>(key, value) {
  override val type: SettingType
    get() = SettingType.BOOLEAN

  override fun toString(): String {
    return "BooleanSetting(key=$key, value=$value)"
  }
}
