package xyz.poeschl.pathseeker.controller

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.apache.commons.lang3.EnumUtils
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import xyz.poeschl.pathseeker.exceptions.InvalidConfigKeyException
import xyz.poeschl.pathseeker.models.settings.SaveSettingDto
import xyz.poeschl.pathseeker.models.settings.Setting
import xyz.poeschl.pathseeker.models.settings.SettingKey
import xyz.poeschl.pathseeker.security.repository.User
import xyz.poeschl.pathseeker.service.ConfigService

@RestController
@RequestMapping("/config")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('${User.ROLE_ADMIN}')")
class ConfigRestController(private val configService: ConfigService) {

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
}
