package xyz.poeschl.roborush.security.restcontroller

import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import xyz.poeschl.roborush.models.settings.SettingKey
import xyz.poeschl.roborush.security.service.UserDetailsService
import xyz.poeschl.roborush.security.utils.JwtTokenProvider
import xyz.poeschl.roborush.service.ConfigService

@RestController
@RequestMapping("/auth")
class AuthRestController(
  private val authenticationManager: AuthenticationManager,
  private val jwtTokenProvider: JwtTokenProvider,
  private val userDetailsService: UserDetailsService,
  private val configService: ConfigService
) {

  companion object {
    private val LOGGER = LoggerFactory.getLogger(AuthRestController::class.java)
  }

  @PostMapping("/login", produces = [MediaType.APPLICATION_JSON_VALUE], consumes = [MediaType.APPLICATION_JSON_VALUE])
  fun authenticateUser(@RequestBody loginRequest: LoginRequest): ResponseEntity<LoginResponse> {
    val authentication: Authentication = authenticationManager.authenticate(
      UsernamePasswordAuthenticationToken(
        loginRequest.username,
        loginRequest.password
      )
    )
    SecurityContextHolder.getContext().authentication = authentication
    val jwt: String = jwtTokenProvider.createToken(authentication)
    return ResponseEntity.ok(LoginResponse(jwt))
  }

  @PostMapping("/register", produces = [MediaType.APPLICATION_JSON_VALUE], consumes = [MediaType.APPLICATION_JSON_VALUE])
  fun registerUser(@RequestBody registerRequest: RegisterRequest): ResponseEntity<Void> {
    if (registerRequest.username.length > 3 &&
      registerRequest.password.length > 8 &&
      configService.getBooleanSetting(SettingKey.ENABLE_USER_REGISTRATION).value
    ) {
      try {
        userDetailsService.registerNewUser(registerRequest.username, registerRequest.password)
      } catch (ex: DataIntegrityViolationException) {
        LOGGER.warn("Username '${registerRequest.username}' is already taken. Registration not possible")
        return ResponseEntity.status(HttpStatus.CONFLICT).build()
      }
      return ResponseEntity.ok().build()
    } else {
      return ResponseEntity.badRequest().build()
    }
  }
}
