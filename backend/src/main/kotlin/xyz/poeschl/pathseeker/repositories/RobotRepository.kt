package xyz.poeschl.pathseeker.repositories

import jakarta.persistence.*
import net.karneim.pojobuilder.GeneratePojoBuilder
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import xyz.poeschl.pathseeker.configuration.Builder
import xyz.poeschl.pathseeker.models.Color
import xyz.poeschl.pathseeker.security.repository.User

@Repository
interface RobotRepository : CrudRepository<Robot, Long> {
  fun findRobotByUser(user: User): Robot?
}

@Entity
@GeneratePojoBuilder(withBuilderInterface = Builder::class)
@Table(name = "robots")
data class Robot(
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(insertable = false) val id: Long?,
  @Column(name = "color") val color: Color,
  @OneToOne val user: User
)