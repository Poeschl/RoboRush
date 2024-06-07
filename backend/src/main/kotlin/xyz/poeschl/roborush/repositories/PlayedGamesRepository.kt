package xyz.poeschl.roborush.repositories

import jakarta.persistence.*
import net.karneim.pojobuilder.GeneratePojoBuilder
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import xyz.poeschl.roborush.configuration.Builder
import java.time.ZonedDateTime

@Repository
interface PlayedGamesRepository : CrudRepository<PlayedGame, Long> {

  @Query("Select pg from PlayedGame pg where pg.endedAt > :until")
  fun getPlayedGamesUntil(@Param("until") until: ZonedDateTime): List<PlayedGame>
}

@Entity
@GeneratePojoBuilder(withBuilderInterface = Builder::class)
@Table(name = "played_games")
data class PlayedGame(
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(insertable = false) val id: Long?,
  @OneToOne val winnerRobot: Robot?,
  @Column val turnsTaken: Int,
  @CreatedDate @Column(name = "ended_at") var endedAt: ZonedDateTime = ZonedDateTime.now()
)
