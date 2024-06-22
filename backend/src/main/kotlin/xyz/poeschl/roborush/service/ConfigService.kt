package xyz.poeschl.roborush.service

import org.springframework.cache.annotation.CacheEvict
import org.springframework.stereotype.Service
import xyz.poeschl.roborush.controller.WebsocketController
import xyz.poeschl.roborush.models.settings.*
import xyz.poeschl.roborush.repositories.ConfigRepository
import xyz.poeschl.roborush.repositories.SettingEntity

@Service
class ConfigService(
  private val configRepository: ConfigRepository,
  private val settingsEntityMapper: SettingEntityMapper,
  private val websocketController: WebsocketController
) {

  private var globalNotificationText = ""

  @CacheEvict(cacheNames = ["gameInfoCache"], allEntries = true)
  fun saveSetting(settingDto: SaveSettingDto): Setting<*> {
    val existingEntity: SettingEntity = configRepository.findByKey(settingDto.key)
    val updatedEntity = settingsEntityMapper.toEntity(existingEntity, settingDto)

    val saved = settingsEntityMapper.fromEntity(configRepository.save(updatedEntity))

    if (saved.key.isFrontendSetting) {
      sendClientConfigUpdate()
    }

    return saved
  }

  fun getAllSettings(): List<Setting<*>> {
    return configRepository.findAll().map { settingsEntityMapper.fromEntity(it) }.sortedByDescending { it.key }
  }

  fun getIntSetting(key: SettingKey): IntSetting {
    return getSetting(key) as IntSetting
  }

  fun getDurationSetting(key: SettingKey): DurationSetting {
    return getSetting(key) as DurationSetting
  }

  fun getBooleanSetting(key: SettingKey): BooleanSetting {
    return getSetting(key) as BooleanSetting
  }

  fun getSetting(key: SettingKey): Setting<*> {
    val entity: SettingEntity = configRepository.findByKey(key)
    return settingsEntityMapper.fromEntity(entity)
  }

  fun setGlobalNotificationText(text: String) {
    this.globalNotificationText = text
    sendClientConfigUpdate()
  }

  fun getGlobalNotificationText(): String = globalNotificationText

  private fun sendClientConfigUpdate() {
    websocketController.sendClientSettingsUpdate(
      ClientSettings(
        globalNotificationText,
        getBooleanSetting(SettingKey.ENABLE_FOG_OF_WAR).value,
        getBooleanSetting(SettingKey.ENABLE_WEB_ROBOT_CONTROL).value
      )
    )
  }
}
