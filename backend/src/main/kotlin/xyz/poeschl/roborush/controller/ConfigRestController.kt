package xyz.poeschl.roborush.controller

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.apache.commons.lang3.EnumUtils
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import xyz.poeschl.roborush.controller.restmodels.MapActiveDto
import xyz.poeschl.roborush.controller.restmodels.MapAttributeSaveDto
import xyz.poeschl.roborush.controller.restmodels.MapGenerationResult
import xyz.poeschl.roborush.controller.restmodels.TextConfigDto
import xyz.poeschl.roborush.exceptions.InvalidConfigKeyException
import xyz.poeschl.roborush.exceptions.InvalidHeightMapException
import xyz.poeschl.roborush.exceptions.MapNotFound
import xyz.poeschl.roborush.models.settings.ClientSettings
import xyz.poeschl.roborush.models.settings.SaveSettingDto
import xyz.poeschl.roborush.models.settings.Setting
import xyz.poeschl.roborush.models.settings.SettingKey
import xyz.poeschl.roborush.repositories.Map
import xyz.poeschl.roborush.security.repository.User
import xyz.poeschl.roborush.service.ConfigService
import xyz.poeschl.roborush.service.MapService

@RestController
@RequestMapping("/config")
class ConfigRestController(private val configService: ConfigService, private val mapService: MapService) {

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
    val mapGenResult = mapService.createNewMapFromHeightMap(name, heightMapFile.inputStream)
    mapService.saveMap(mapGenResult.map)

    return MapGenerationResult(mapGenResult.errors)
  }

  @SecurityRequirement(name = "Bearer Authentication")
  @PreAuthorize("hasRole('${User.ROLE_ADMIN}')")
  @GetMapping("/map", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getMaps(): List<Map> {
    return mapService.getAllMaps()
  }

  @SecurityRequirement(name = "Bearer Authentication")
  @PreAuthorize("hasRole('${User.ROLE_ADMIN}')")
  @PostMapping("/map/{id}/active", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
  fun setMapActive(@PathVariable id: Long, @RequestBody activeDto: MapActiveDto): Map {
    val map = mapService.getMap(id)

    if (map != null) {
      return mapService.setMapActive(map, activeDto.active)
    } else {
      throw MapNotFound("No matching map found for setting active")
    }
  }

  @SecurityRequirement(name = "Bearer Authentication")
  @PreAuthorize("hasRole('${User.ROLE_ADMIN}')")
  @PostMapping("/map/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
  fun setMapAttributes(@PathVariable id: Long, @RequestBody attributes: MapAttributeSaveDto): Map {
    val map = mapService.getMap(id)

    if (map != null) {
      return mapService.setMapAttributes(map, attributes)
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
  @PostMapping("/client/globalNotificationText", consumes = [MediaType.APPLICATION_JSON_VALUE])
  fun setGlobalNotificationText(@RequestBody dto: TextConfigDto) {
    configService.setGlobalNotificationText(dto.text)
  }

  @GetMapping("/client", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getClientSettings(): ClientSettings {
    return ClientSettings(configService.getGlobalNotificationText())
  }
}
