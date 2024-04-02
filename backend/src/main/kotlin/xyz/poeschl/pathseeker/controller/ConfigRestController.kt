package xyz.poeschl.pathseeker.controller

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.apache.commons.lang3.EnumUtils
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import xyz.poeschl.pathseeker.controller.restmodels.MapGenerationResult
import xyz.poeschl.pathseeker.exceptions.InvalidConfigKeyException
import xyz.poeschl.pathseeker.exceptions.InvalidHeightMapException
import xyz.poeschl.pathseeker.models.settings.SaveSettingDto
import xyz.poeschl.pathseeker.models.settings.Setting
import xyz.poeschl.pathseeker.models.settings.SettingKey
import xyz.poeschl.pathseeker.security.repository.User
import xyz.poeschl.pathseeker.service.ConfigService
import xyz.poeschl.pathseeker.service.MapService

@RestController
@RequestMapping("/config")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('${User.ROLE_ADMIN}')")
class ConfigRestController(private val configService: ConfigService, private val mapService: MapService) {

  companion object {
    private val LOGGER = LoggerFactory.getLogger(ConfigRestController::class.java)
  }

  @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getAll(): List<Setting<*>> = configService.getAllSettings()

  @GetMapping("/{key}", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getSingle(@PathVariable key: String): Setting<*> {
    if (EnumUtils.isValidEnum(SettingKey::class.java, key)) {
      return configService.getSetting(SettingKey.valueOf(key))
    } else {
      throw InvalidConfigKeyException("Key '$key' not know by the application")
    }
  }

  @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
  fun saveSingle(@RequestBody setting: SaveSettingDto): Setting<*> {
    return configService.saveSetting(setting)
  }

  @PostMapping("/map/heightmap", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
  fun newHeightMap(@RequestParam("heightmap") heightMapFile: MultipartFile): MapGenerationResult {
    if (heightMapFile.contentType != MediaType.IMAGE_PNG_VALUE) {
      throw InvalidHeightMapException("Only png files are supported for heightmaps")
    }
    val name = heightMapFile.originalFilename?.substringBeforeLast("/")?.substringBeforeLast(".") ?: "unknown"
    val happenedErrors = mapService.newMapFromHeightMap(name, heightMapFile.inputStream)

    return MapGenerationResult(happenedErrors)
  }
}
