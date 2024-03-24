package xyz.poeschl.pathseeker.models

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import xyz.poeschl.pathseeker.repositories.SettingEntity
import xyz.poeschl.pathseeker.test.utils.builder.Builders.Companion.a
import xyz.poeschl.pathseeker.test.utils.builder.NativeTypes.Companion.`$Id`
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class SettingEntityMapperTest {

  private val settingEntityMapper = SettingEntityMapper()

  @Test
  fun toEntity_int() {
    // WHEN
    val id = a(`$Id`())
    val setting = IntSetting(SettingKey.entries.random(), 20)

    // THEN
    val entity = settingEntityMapper.toEntity(setting, id)

    // VERIFY
    assertThat(entity.id).isEqualTo(id)
    assertThat(entity.key).isEqualTo(setting.key)
    assertThat(entity.type).isEqualTo(SettingType.INT)
    assertThat(entity.value).isEqualTo(setting.value.toString())
  }

  @Test
  fun toEntity_duration() {
    // WHEN
    val id = a(`$Id`())
    val setting = DurationSetting(SettingKey.entries.random(), 1.minutes)

    // THEN
    val entity = settingEntityMapper.toEntity(setting, id)

    // VERIFY
    assertThat(entity.id).isEqualTo(id)
    assertThat(entity.key).isEqualTo(setting.key)
    assertThat(entity.type).isEqualTo(SettingType.DURATION)
    assertThat(entity.value).isEqualTo(setting.value.toIsoString())
  }

  @Test
  fun fromEntity_int() {
    // WHEN
    val settingEntity = SettingEntity(a(`$Id`()), SettingKey.entries.random(), SettingType.INT, "20")

    // THEN
    val setting = settingEntityMapper.fromEntity(settingEntity) as IntSetting

    // VERIFY
    assertThat(setting.key).isEqualTo(settingEntity.key)
    assertThat(setting.type).isEqualTo(SettingType.INT)
    assertThat(setting.value).isEqualTo(settingEntity.value.toInt())
  }

  @Test
  fun fromEntity_duration() {
    // WHEN
    val settingEntity = SettingEntity(a(`$Id`()), SettingKey.entries.random(), SettingType.DURATION, "PT2M")

    // THEN
    val setting = settingEntityMapper.fromEntity(settingEntity) as DurationSetting

    // VERIFY
    assertThat(setting.key).isEqualTo(settingEntity.key)
    assertThat(setting.type).isEqualTo(SettingType.DURATION)
    assertThat(setting.value).isEqualTo(Duration.parseIsoString(settingEntity.value))
  }
}
