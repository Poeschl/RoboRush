package xyz.poeschl.pathseeker.repositories

import jakarta.persistence.*
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import xyz.poeschl.pathseeker.models.Color

@Repository
interface RobotRepository : CrudRepository<Robot, Long>

@Entity
@Table(name = "robots")
data class Robot(
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(insertable = false) val id: Long?,
  @Column(name = "color") val color: Color
)
