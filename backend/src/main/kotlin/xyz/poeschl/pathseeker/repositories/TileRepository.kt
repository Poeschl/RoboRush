package xyz.poeschl.pathseeker.repositories

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import net.karneim.pojobuilder.GeneratePojoBuilder
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import xyz.poeschl.pathseeker.configuration.Builder
import xyz.poeschl.pathseeker.models.Position
import xyz.poeschl.pathseeker.models.TileType

@Repository
interface TileRepository : CrudRepository<Tile, Long>

@Entity
@GeneratePojoBuilder(withBuilderInterface = Builder::class)
data class Tile(
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(insertable = false) val id: Long?,
  @Column val position: Position,
  @Column val height: Int = 0,
  @Column @Enumerated(EnumType.STRING) val type: TileType = TileType.DEFAULT_TILE
) {

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "map_id")
  lateinit var map: Map
}
