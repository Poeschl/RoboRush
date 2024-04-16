package xyz.poeschl.pathseeker.repositories

import jakarta.persistence.*
import net.karneim.pojobuilder.GeneratePojoBuilder
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import xyz.poeschl.pathseeker.configuration.Builder
import xyz.poeschl.pathseeker.models.Position
import xyz.poeschl.pathseeker.models.Size

@Repository
interface MapRepository : CrudRepository<Map, Long> {
  fun findAllByOrderById(): List<Map>

  fun findAllByActiveIsTrueOrderById(): List<Map>
}

@Entity
@GeneratePojoBuilder(withBuilderInterface = Builder::class)
data class Map(
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(insertable = false) val id: Long?,
  @Column val mapName: String,
  @Column val size: Size,
  @Column val possibleStartPositions: List<Position>,
  @Column val targetPosition: Position,
  @Column var maxRobotFuel: Int,
  @Column var active: Boolean = false
) {

  @OneToMany(mappedBy = "map", cascade = [CascadeType.ALL])
  val mapData: List<Tile> = mutableListOf()

  fun addTile(tile: Tile) {
    tile.map = this
    (mapData as MutableList).add(tile)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Map

    if (id != other.id) return false
    if (mapName != other.mapName) return false
    if (size != other.size) return false
    if (mapData != other.mapData) return false
    if (possibleStartPositions != other.possibleStartPositions) return false
    if (targetPosition != other.targetPosition) return false
    if (active != other.active) return false

    return true
  }

  override fun hashCode(): Int {
    var result = id?.hashCode() ?: 0
    result = 31 * result + mapName.hashCode()
    result = 31 * result + size.hashCode()
    result = 31 * result + mapData.hashCode()
    result = 31 * result + possibleStartPositions.hashCode()
    result = 31 * result + targetPosition.hashCode()
    result = 31 * result + active.hashCode()
    return result
  }
}
