package xyz.poeschl.pathseeker.repositories

import jakarta.persistence.*
import net.karneim.pojobuilder.GeneratePojoBuilder
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import xyz.poeschl.pathseeker.configuration.Builder
import xyz.poeschl.pathseeker.models.SettingKey
import xyz.poeschl.pathseeker.models.SettingType

@Repository
interface ConfigRepository : CrudRepository<SettingEntity, Long> {

  fun save(setting: SettingEntity): SettingEntity

  fun findByKey(key: SettingKey): SettingEntity
}

@Entity
@GeneratePojoBuilder(withBuilderInterface = Builder::class)
@Table(name = "config")
data class SettingEntity(
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(insertable = false) val id: Long,
  @Column @Enumerated(EnumType.STRING) val key: SettingKey,
  @Column @Enumerated(EnumType.STRING) val type: SettingType,
  @Column val value: String
)
