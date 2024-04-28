package xyz.poeschl.pathseeker.controller.restmodels

data class MapGenerationResult(val warnings: List<String>)

data class MapActiveDto(val active: Boolean)

data class MapAttributeSaveDto(val mapName: String, val maxRobotFuel: Int, val solarChargeRate: Double)
