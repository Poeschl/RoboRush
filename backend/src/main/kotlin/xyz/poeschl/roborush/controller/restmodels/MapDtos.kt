package xyz.poeschl.roborush.controller.restmodels

import xyz.poeschl.roborush.models.Position
import xyz.poeschl.roborush.models.Size
import xyz.poeschl.roborush.repositories.Map
import xyz.poeschl.roborush.repositories.Tile

data class MapGenerationResult(val warnings: List<String>)

data class MapActiveDto(val active: Boolean)

data class MapAttributeSaveDto(val mapName: String, val maxRobotFuel: Int, val solarChargeRate: Double)

data class PlaygroundMap(
  val id: Long,
  val mapName: String,
  val size: Size,
  val possibleStartPositions: List<Position>,
  val targetPosition: Position,
  val maxRobotFuel: Int = 300,
  val solarChargeRate: Double = 0.0,
  val active: Boolean = false,
  val mapData: List<Tile> = mutableListOf(),
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
    map.mapData,
    minMax.first,
    minMax.second
  )
}
