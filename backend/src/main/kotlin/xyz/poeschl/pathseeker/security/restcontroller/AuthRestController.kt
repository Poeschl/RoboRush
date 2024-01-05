package xyz.poeschl.pathseeker.security.restcontroller

import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import xyz.poeschl.pathseeker.security.UserDetailsService
import xyz.poeschl.pathseeker.security.utils.JwtTokenProvider

@RestController
@RequestMapping("/auth")
class AuthRestController(
  private val authenticationManager: AuthenticationManager,
  private val jwtTokenProvider: JwtTokenProvider,
  private val userDetailsService: UserDetailsService
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
    try {
      userDetailsService.registerNewUser(registerRequest.username, registerRequest.password)
    } catch (ex: DataIntegrityViolationException) {
      LOGGER.warn("Username '${registerRequest.username}' is already taken. Registration not possible")
      return ResponseEntity.status(HttpStatus.CONFLICT).build()
    }
    return ResponseEntity.ok().build()
  }
}
