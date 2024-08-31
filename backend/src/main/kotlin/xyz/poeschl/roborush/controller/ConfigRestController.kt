package xyz.poeschl.roborush.controller

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.apache.commons.lang3.EnumUtils
import org.slf4j.LoggerFactory
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import xyz.poeschl.roborush.controller.restmodels.*
import xyz.poeschl.roborush.exceptions.InvalidConfigKeyException
import xyz.poeschl.roborush.exceptions.InvalidHeightMapException
import xyz.poeschl.roborush.exceptions.MapNotFound
import xyz.poeschl.roborush.models.settings.ClientSettings
import xyz.poeschl.roborush.models.settings.SaveSettingDto
import xyz.poeschl.roborush.models.settings.Setting
import xyz.poeschl.roborush.models.settings.SettingKey
import xyz.poeschl.roborush.repositories.Tile
import xyz.poeschl.roborush.security.repository.User
import xyz.poeschl.roborush.service.ConfigService
import xyz.poeschl.roborush.service.MapImportExportService
import xyz.poeschl.roborush.service.MapService

@RestController
@RequestMapping("/config")
class ConfigRestController(
  private val configService: ConfigService,
  private val mapService: MapService,
  private val mapImportExportService: MapImportExportService
) {

  companion object {
    private val LOGGER = LoggerFactory.getLogger(ConfigRestController::class.java)
  }

  @SecurityRequirement(name = "Bearer Authentication")
  @PreAuthorize("hasRole('${User.ROLE_ADMIN}')")
  @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getAll(): List<Setting<*>> = configService.getAllSettings()

  @SecurityRequirement(name = "Bearer Authentication")
  @PreAuthorize("hasRole('${User.ROLE_ADMIN}')")
  @GetMapping("/{key}", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getSingle(@PathVariable key: String): Setting<*> {
    if (EnumUtils.isValidEnum(SettingKey::class.java, key)) {
      return configService.getSetting(SettingKey.valueOf(key))
    } else {
      throw InvalidConfigKeyException("Key '$key' not know by the application")
    }
  }

  @SecurityRequirement(name = "Bearer Authentication")
  @PreAuthorize("hasRole('${User.ROLE_ADMIN}')")
  @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
  fun saveSingle(@RequestBody setting: SaveSettingDto): Setting<*> {
    return configService.saveSetting(setting)
  }

  @SecurityRequirement(name = "Bearer Authentication")
  @PreAuthorize("hasRole('${User.ROLE_ADMIN}')")
  @PostMapping("/map/heightmap", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
  fun newHeightMap(@RequestParam("heightmap") heightMapFile: MultipartFile): MapGenerationResult {
    if (heightMapFile.contentType != MediaType.IMAGE_PNG_VALUE) {
      throw InvalidHeightMapException("Only png files are supported for heightmaps")
    }
    val name = heightMapFile.originalFilename?.substringBeforeLast("/")?.substringBeforeLast(".") ?: "unknown"
    val mapGenResult = mapImportExportService.importMap(name, heightMapFile.inputStream)
    mapService.saveMap(mapGenResult.map)

    return MapGenerationResult(mapGenResult.errors)
  }

  @SecurityRequirement(name = "Bearer Authentication")
  @PreAuthorize("hasRole('${User.ROLE_ADMIN}')")
  @GetMapping("/map", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getMaps(): List<PlaygroundMap> {
    return mapService.getAllMaps().map {
      val minMax = Pair(it.mapData.minOf { mapTile -> mapTile.height }, it.mapData.maxOf { mapTile -> mapTile.height })
      return@map PlaygroundMap(it, minMax)
    }
  }

  @SecurityRequirement(name = "Bearer Authentication")
  @PreAuthorize("hasRole('${User.ROLE_ADMIN}')")
  @PostMapping("/map/{id}/active", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
  fun setMapActive(@PathVariable id: Long, @RequestBody activeDto: MapActiveDto): PlaygroundMap {
    val map = mapService.getMap(id)

    if (map != null) {
      val minMax = Pair(map.mapData.minOf { it.height }, map.mapData.maxOf { it.height })
      return PlaygroundMap(mapService.setMapActive(map, activeDto.active), minMax)
    } else {
      throw MapNotFound("No matching map found for setting active")
    }
  }

  @SecurityRequirement(name = "Bearer Authentication")
  @PreAuthorize("hasRole('${User.ROLE_ADMIN}')")
  @PostMapping("/map/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
  fun setMapAttributes(@PathVariable id: Long, @RequestBody attributes: MapAttributeSaveDto): PlaygroundMap {
    val map = mapService.getMap(id)

    if (map != null) {
      val minMax = Pair(map.mapData.minOf { it.height }, map.mapData.maxOf { it.height })
      return PlaygroundMap(mapService.setMapAttributes(map, attributes), minMax)
    } else {
      throw MapNotFound("No matching map found for given id.")
    }
  }

  @SecurityRequirement(name = "Bearer Authentication")
  @PreAuthorize("hasRole('${User.ROLE_ADMIN}')")
  @PostMapping("/map/{id}/tile", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
  fun setMapTile(@PathVariable id: Long, @RequestBody tile: Tile): PlaygroundMap {
    val map = mapService.getMap(id)

    if (map != null) {
      val minMax = Pair(map.mapData.minOf { it.height }, map.mapData.maxOf { it.height })
      return PlaygroundMap(mapService.setMapTile(map, tile), minMax)
    } else {
      throw MapNotFound("No matching map found for given id.")
    }
  }

  @SecurityRequirement(name = "Bearer Authentication")
  @PreAuthorize("hasRole('${User.ROLE_ADMIN}')")
  @DeleteMapping("/map/{id}")
  fun removeMap(@PathVariable id: Long) {
    val map = mapService.getMap(id)

    if (map != null) {
      return mapService.deleteMap(map)
    } else {
      throw MapNotFound("No matching map found for deletion")
    }
  }

  @SecurityRequirement(name = "Bearer Authentication")
  @PreAuthorize("hasRole('${User.ROLE_ADMIN}')")
  @GetMapping("/map/{id}/export", produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
  fun exportMap(@PathVariable id: Long): ResponseEntity<Resource> {
    val map = mapService.getMap(id)

    if (map != null) {
      val resource = ByteArrayResource(mapImportExportService.exportMap(map).toByteArray())

      return ResponseEntity.ok()
        .contentLength(resource.contentLength())
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(resource)
    } else {
      throw MapNotFound("No matching map found for deletion")
    }
  }

  @SecurityRequirement(name = "Bearer Authentication")
  @PreAuthorize("hasRole('${User.ROLE_ADMIN}')")
  @PostMapping("/client/globalNotificationText", consumes = [MediaType.APPLICATION_JSON_VALUE])
  fun setGlobalNotificationText(@RequestBody dto: TextConfigDto) {
    configService.setGlobalNotificationText(dto.text)
  }

  @GetMapping("/client", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getClientSettings(): ClientSettings {
    return ClientSettings(
      configService.getGlobalNotificationText(),
      configService.getBooleanSetting(SettingKey.ENABLE_WEB_ROBOT_CONTROL).value,
      configService.getBooleanSetting(SettingKey.ENABLE_USER_REGISTRATION).value
    )
  }
}
