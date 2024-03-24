package xyz.poeschl.pathseeker.service

import org.springframework.stereotype.Service
import xyz.poeschl.pathseeker.models.*
import xyz.poeschl.pathseeker.repositories.ConfigRepository
import xyz.poeschl.pathseeker.repositories.SettingEntity

@Service
class ConfigService(
  private val configRepository: ConfigRepository,
  private val settingsEntityMapper: SettingEntityMapper
) {

  fun <T> saveSetting(setting: Setting<T>) {
    val existingEntity: SettingEntity = configRepository.findByKey(setting.key)
    val updatedEntity = settingsEntityMapper.toEntity(setting, existingEntity.id)

    return configRepository.save(updatedEntity)
  }

  fun getIntSetting(key: SettingKey): IntSetting {
    return getSetting(key) as IntSetting
  }

  fun getDurationSetting(key: SettingKey): DurationSetting {
    return getSetting(key) as DurationSetting
  }

  private fun getSetting(key: SettingKey): Setting<*> {
    val entity: SettingEntity = configRepository.findByKey(key)
    return settingsEntityMapper.fromEntity(entity)
  }
}
