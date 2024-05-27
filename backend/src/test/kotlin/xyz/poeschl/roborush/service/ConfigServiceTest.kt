package xyz.poeschl.roborush.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import xyz.poeschl.roborush.controller.WebsocketController
import xyz.poeschl.roborush.models.settings.*
import xyz.poeschl.roborush.repositories.ConfigRepository
import xyz.poeschl.roborush.test.utils.builder.Builders.Companion.a
import xyz.poeschl.roborush.test.utils.builder.ConfigTypes.Companion.`$SettingEntity`
import xyz.poeschl.roborush.test.utils.builder.NativeTypes.Companion.`$Int`
import xyz.poeschl.roborush.test.utils.builder.NativeTypes.Companion.`$String`
import kotlin.time.Duration.Companion.minutes

class ConfigServiceTest {

  private val configRepository = mockk<ConfigRepository>()
  private val settingEntityMapper = mockk<SettingEntityMapper>()
  private val websocketController = mockk<WebsocketController>(relaxUnitFun = true)
  private val configService = ConfigService(configRepository, settingEntityMapper, websocketController)

  @Test
  fun saveSetting() {
    // WHEN
    val settingEntity = a(`$SettingEntity`().withKey(SettingKey.TIMEOUT_GAME_END).withType(SettingType.INT).withValue("40"))
    val convertedEntity = a(`$SettingEntity`().withType(SettingType.INT).withKey(settingEntity.key).withValue("50"))
    val settingDto = SaveSettingDto(settingEntity.key, "30")

    every { configRepository.findByKey(settingEntity.key) } returns settingEntity
    every { settingEntityMapper.toEntity(settingEntity, settingDto) } returns convertedEntity
    every { configRepository.save(convertedEntity) } returns convertedEntity
    every { settingEntityMapper.fromEntity(any()) } answers { callOriginal() }

    // THEN
    val setting = configService.saveSetting(settingDto)

    // VERIFY
    assertThat(setting.key).isEqualTo(settingEntity.key)
    assertThat(setting.type).isEqualTo(SettingType.INT)
    assertThat(setting.value).isEqualTo(50)

    verify(exactly = 0) { websocketController.sendClientSettingsUpdate(any()) }
  }

  @Test
  fun saveSetting_frontendSetting() {
    // WHEN
    val settingEntity = a(`$SettingEntity`().withKey(SettingKey.USE_FOG_OF_WAR).withType(SettingType.BOOLEAN).withValue("true"))
    val convertedEntity = a(`$SettingEntity`().withKey(SettingKey.USE_FOG_OF_WAR).withType(SettingType.BOOLEAN).withValue("true"))
    val settingDto = SaveSettingDto(settingEntity.key, "true")

    every { configRepository.findByKey(settingEntity.key) } returns settingEntity
    every { settingEntityMapper.toEntity(settingEntity, settingDto) } returns convertedEntity
    every { configRepository.save(convertedEntity) } returns convertedEntity
    every { settingEntityMapper.fromEntity(any()) } answers { callOriginal() }

    // THEN
    val setting = configService.saveSetting(settingDto)

    // VERIFY
    assertThat(setting.key).isEqualTo(settingEntity.key)
    assertThat(setting.type).isEqualTo(SettingType.BOOLEAN)
    assertThat(setting.value).isEqualTo(true)

    verify(exactly = 1) { websocketController.sendClientSettingsUpdate(any()) }
  }

  @Test
  fun getAllSettings() {
    // WHEN
    val settingBuilder = `$SettingEntity`().withType(SettingType.INT).withValue(a(`$Int`()).toString())
    val settingEntities = listOf(a(settingBuilder), a(settingBuilder.but().withValue(a(`$Int`()).toString())))
    val expectedSettings = settingEntities.map { IntSetting(it.key, it.value.toInt()) }

    every { configRepository.findAll() } returns settingEntities
    every { settingEntityMapper.fromEntity(any()) } answers { callOriginal() }

    // THEN
    val settings = configService.getAllSettings()

    // VERIFY
    assertThat(settings).containsAnyElementsOf(expectedSettings)
  }

  @Test
  fun getIntSetting() {
    // WHEN
    val settingEntity = a(`$SettingEntity`().withType(SettingType.INT).withValue(a(`$Int`()).toString()))
    val expected = IntSetting(settingEntity.key, settingEntity.value.toInt())
    val searchKey = SettingKey.TIMEOUT_GAME_END

    every { configRepository.findByKey(searchKey) } returns settingEntity
    every { settingEntityMapper.fromEntity(any()) } answers { callOriginal() }

    // THEN
    val setting = configService.getIntSetting(searchKey)

    // VERIFY
    assertThat(setting).isEqualTo(expected)
  }

  @Test
  fun getDurationSetting() {
    // WHEN
    val settingEntity = a(`$SettingEntity`().withType(SettingType.DURATION).withValue("PT5M"))
    val expected = DurationSetting(settingEntity.key, 5.minutes)
    val searchKey = SettingKey.TIMEOUT_WAIT_FOR_PLAYERS

    every { configRepository.findByKey(searchKey) } returns settingEntity
    every { settingEntityMapper.fromEntity(any()) } answers { callOriginal() }

    // THEN
    val setting = configService.getDurationSetting(searchKey)

    // VERIFY
    assertThat(setting).isEqualTo(expected)
  }

  @Test
  fun getBooleanSetting() {
    // WHEN
    val settingEntity = a(`$SettingEntity`().withType(SettingType.BOOLEAN).withValue("true"))
    val expected = BooleanSetting(settingEntity.key, true)
    val searchKey = SettingKey.TARGET_POSITION_IN_GAMEINFO

    every { configRepository.findByKey(searchKey) } returns settingEntity
    every { settingEntityMapper.fromEntity(any()) } answers { callOriginal() }

    // THEN
    val setting = configService.getBooleanSetting(searchKey)

    // VERIFY
    assertThat(setting).isEqualTo(expected)
  }

  @Test
  fun getSetting() {
    // WHEN
    val settingEntity = a(`$SettingEntity`().withType(SettingType.DURATION).withValue("PT10M"))
    val expected = DurationSetting(settingEntity.key, 10.minutes)
    val searchKey = SettingKey.TIMEOUT_WAIT_FOR_PLAYERS

    every { configRepository.findByKey(searchKey) } returns settingEntity
    every { settingEntityMapper.fromEntity(any()) } answers { callOriginal() }

    // THEN
    val setting = configService.getDurationSetting(searchKey)

    // VERIFY
    assertThat(setting).isEqualTo(expected)
  }

  @Test
  fun setGlobalNotificationText() {
    // WHEN
    val text = a(`$String`("text"))
    val settingEntity = a(`$SettingEntity`().withKey(SettingKey.USE_FOG_OF_WAR).withType(SettingType.BOOLEAN).withValue("true"))

    every { configRepository.findByKey(SettingKey.USE_FOG_OF_WAR) } returns settingEntity
    every { settingEntityMapper.fromEntity(settingEntity) } answers { callOriginal() }

    // THEN
    configService.setGlobalNotificationText(text)

    // VERIFY
    assertThat(configService.getGlobalNotificationText()).isEqualTo(text)
    verify { websocketController.sendClientSettingsUpdate(any()) }
  }
}
