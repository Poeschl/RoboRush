package xyz.poeschl.roborush.models.settings

import net.karneim.pojobuilder.GeneratePojoBuilder
import xyz.poeschl.roborush.configuration.Builder

class BooleanSetting
@GeneratePojoBuilder(withBuilderInterface = Builder::class)
constructor(key: SettingKey, value: Boolean) : Setting<Boolean>(key, value) {
  override val type: SettingType
    get() = SettingType.BOOLEAN

  override fun toString(): String = "BooleanSetting(key=$key, value=$value)"

  fun invertedValue() = !value
}
