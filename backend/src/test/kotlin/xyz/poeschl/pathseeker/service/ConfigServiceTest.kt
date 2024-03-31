package xyz.poeschl.pathseeker.service

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import xyz.poeschl.pathseeker.models.*
import xyz.poeschl.pathseeker.repositories.ConfigRepository
import xyz.poeschl.pathseeker.test.utils.builder.Builders.Companion.a
import xyz.poeschl.pathseeker.test.utils.builder.ConfigTypes.Companion.`$SettingEntity`
import xyz.poeschl.pathseeker.test.utils.builder.NativeTypes.Companion.`$Int`
import kotlin.time.Duration.Companion.minutes

class ConfigServiceTest {

  private val configRepository = mockk<ConfigRepository>()
  private val settingEntityMapper = mockk<SettingEntityMapper>()
  private val configService = ConfigService(configRepository, settingEntityMapper)

  @Test
  fun saveSetting() {
    // WHEN
    val settingEntity = a(`$SettingEntity`().withType(SettingType.INT).withValue("40"))
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
    assertThat(setting.value).isEqualTo(settingDto.value)
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
}
