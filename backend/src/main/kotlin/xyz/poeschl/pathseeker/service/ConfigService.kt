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

  fun saveSetting(settingDto: SaveSettingDto): Setting<*> {
    val existingEntity: SettingEntity = configRepository.findByKey(settingDto.key)
    val updatedEntity = settingsEntityMapper.toEntity(existingEntity, settingDto)

    return settingsEntityMapper.fromEntity(configRepository.save(updatedEntity))
  }

  fun getAllSettings(): List<Setting<*>> {
    return configRepository.findAll().map { settingsEntityMapper.fromEntity(it) }
  }

  fun getIntSetting(key: SettingKey): IntSetting {
    return getSetting(key) as IntSetting
  }

  fun getDurationSetting(key: SettingKey): DurationSetting {
    return getSetting(key) as DurationSetting
  }

  fun getSetting(key: SettingKey): Setting<*> {
    val entity: SettingEntity = configRepository.findByKey(key)
    return settingsEntityMapper.fromEntity(entity)
  }
}
