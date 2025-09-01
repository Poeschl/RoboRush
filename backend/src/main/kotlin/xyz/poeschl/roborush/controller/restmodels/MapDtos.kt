package xyz.poeschl.roborush.controller.restmodels

import net.karneim.pojobuilder.GeneratePojoBuilder
import xyz.poeschl.roborush.configuration.Builder
import xyz.poeschl.roborush.models.Position
import xyz.poeschl.roborush.models.Size
import xyz.poeschl.roborush.models.TileType
import xyz.poeschl.roborush.repositories.Map

data class MapGenerationResult(val warnings: List<String>)

data class MapActiveDto(val active: Boolean)

@GeneratePojoBuilder(withBuilderInterface = Builder::class)
data class MapAttributeSaveDto(val mapName: String, val maxRobotFuel: Int, val solarChargeRate: Double)

data class TileDTO(
  val position: Position,
  val height: Int = 0,
  val type: TileType = TileType.DEFAULT_TILE
)

data class PlaygroundMap(
  val id: Long,
  val mapName: String,
  val size: Size,
  val possibleStartPositions: List<Position>,
  val targetPosition: Position,
  val maxRobotFuel: Int = 300,
  val solarChargeRate: Double = 0.0,
  val active: Boolean = false,
  val mapData: List<TileDTO> = mutableListOf(),
  val minHeight: Int,
  val maxHeight: Int
) {

  constructor(map: Map, minMax: Pair<Int, Int>) : this(
    map.id!!,
    map.mapName,
    map.size,
    map.possibleStartPositions,
    map.targetPosition,
    map.maxRobotFuel,
    map.solarChargeRate,
    map.active,
    map.mapData.map { TileDTO(it.position, it.height, it.type) },
    minMax.first,
    minMax.second
  )
}
