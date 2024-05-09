package xyz.poeschl.roborush.repositories

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import net.karneim.pojobuilder.GeneratePojoBuilder
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import xyz.poeschl.roborush.configuration.Builder
import xyz.poeschl.roborush.models.Position
import xyz.poeschl.roborush.models.TileType

@Repository
interface TileRepository : CrudRepository<Tile, Long>

@Entity
@GeneratePojoBuilder(withBuilderInterface = Builder::class)
data class Tile(
  @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tile_id_seq")
  @SequenceGenerator(name = "tile_id_seq", sequenceName = "tile_id_seq", allocationSize = 100)
  @Column(insertable = false)
  val id: Long?,
  @Column val position: Position,
  @Column val height: Int = 0,
  @Column @Enumerated(EnumType.STRING) val type: TileType = TileType.DEFAULT_TILE
) {

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "map_id")
  lateinit var map: Map
}
