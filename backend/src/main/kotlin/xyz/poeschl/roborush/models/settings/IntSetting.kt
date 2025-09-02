package xyz.poeschl.roborush.models.settings

import net.karneim.pojobuilder.GeneratePojoBuilder
import xyz.poeschl.roborush.configuration.Builder

class IntSetting
@GeneratePojoBuilder(withBuilderInterface = Builder::class)
constructor(key: SettingKey, value: Int) : Setting<Int>(key, value) {
  override val type: SettingType
    get() = SettingType.INT

  override fun toString(): String = "IntSetting(key=$key, value=$value)"
}
