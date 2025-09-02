package xyz.poeschl.roborush.models.settings

import org.springframework.stereotype.Component
import xyz.poeschl.roborush.repositories.SettingEntity
import kotlin.time.Duration

@Component
class SettingEntityMapper {

  /**
   * Convert a settingInput to a SettingEntity. It also makes sure to get the right type.
   */
  fun toEntity(settingEntity: SettingEntity, settingInput: SaveSettingDto): SettingEntity = when (settingEntity.type) {
    SettingType.INT -> SettingEntity(settingEntity.id, settingEntity.key, settingEntity.type, settingInput.value.toInt().toString())
    SettingType.DURATION -> SettingEntity(settingEntity.id, settingEntity.key, settingEntity.type, Duration.parseIsoString(settingInput.value).toIsoString())
    SettingType.BOOLEAN -> SettingEntity(settingEntity.id, settingEntity.key, settingEntity.type, settingInput.value.toBoolean().toString())
  }

  fun toEntity(setting: Setting<*>, id: Long): SettingEntity = when (setting.type) {
    SettingType.INT -> SettingEntity(id, setting.key, setting.type, (setting.value as Int).toString())
    SettingType.DURATION -> SettingEntity(id, setting.key, setting.type, (setting.value as Duration).toIsoString())
    SettingType.BOOLEAN -> SettingEntity(id, setting.key, setting.type, (setting.value as Boolean).toString())
  }

  fun fromEntity(settingEntity: SettingEntity): Setting<*> = when (settingEntity.type) {
    SettingType.INT -> IntSetting(settingEntity.key, settingEntity.value.toInt())
    SettingType.DURATION -> DurationSetting(settingEntity.key, Duration.parseIsoString(settingEntity.value))
    SettingType.BOOLEAN -> BooleanSetting(settingEntity.key, settingEntity.value.toBoolean())
  }
}
