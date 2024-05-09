package xyz.poeschl.roborush.repositories

import jakarta.persistence.*
import net.karneim.pojobuilder.GeneratePojoBuilder
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import xyz.poeschl.roborush.configuration.Builder
import xyz.poeschl.roborush.models.settings.SettingKey
import xyz.poeschl.roborush.models.settings.SettingType

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
