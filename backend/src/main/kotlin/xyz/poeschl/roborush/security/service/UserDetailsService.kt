package xyz.poeschl.roborush.security.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import xyz.poeschl.roborush.models.Color
import xyz.poeschl.roborush.repositories.Robot
import xyz.poeschl.roborush.repositories.RobotRepository
import xyz.poeschl.roborush.security.repository.User
import xyz.poeschl.roborush.security.repository.UserRepository
import xyz.poeschl.roborush.security.utils.JwtTokenProvider
import java.security.SecureRandom

@Configuration
class UserDetailsService(
  private val userRepository: UserRepository,
  private val robotRepository: RobotRepository,
  private val passwordEncoder: PasswordEncoder,
  private val jwtTokenProvider: JwtTokenProvider
) : UserDetailsService {

  companion object {
    private val LOGGER = LoggerFactory.getLogger(UserDetailsService::class.java)
    private const val INITIAL_ROOT_PASSWORD_LENGTH = 32
  }

  @Value("\${INITIAL_ROOT_PASSWORD:}")
  private val initialRootPassword: String = ""

  override fun loadUserByUsername(username: String): UserDetails {
    return userRepository.findByUsername(username) ?: throw UsernameNotFoundException("User '$username' not found!")
  }

  fun registerNewUser(username: String, password: String) {
    val encodedPassword = passwordEncoder.encode(password)
    val user = userRepository.save(User(username, encodedPassword))
    robotRepository.save(Robot(null, Color.randomColor(), 0, user))
  }

  fun loadUserByToken(token: String): UserDetails? {
    return if (token.isNotBlank() && jwtTokenProvider.validateToken(token)) {
      loadUserByUsername(jwtTokenProvider.getUsername(token))
    } else {
      null
    }
  }

  @EventListener(ApplicationReadyEvent::class)
  fun createAdminIfNotExisting() {
    if (userRepository.findByUsername(User.ROOT_USERNAME) == null) {
      LOGGER.info("No admin user found with name '{}'", User.ROOT_USERNAME)
      val password = initialRootPassword.ifBlank { generateRandomPassword(INITIAL_ROOT_PASSWORD_LENGTH) }
      userRepository.save(User(User.ROOT_USERNAME, passwordEncoder.encode(password)))
      LOGGER.info("################################")
      LOGGER.info("Generated password for user '{}':", User.ROOT_USERNAME)
      LOGGER.info("{}", password)
      LOGGER.info("################################")
    } else {
      LOGGER.debug("Admin user found.")
    }
  }

  private fun generateRandomPassword(length: Int): String {
    val charset = ('a'..'z') + ('A'..'Z') + ('0'..'9') + listOf('!', '@', '#', '$', '%', '?', '&', '*', '+', '-')
    val secureRandom = SecureRandom()
    return (1..length)
      .map { charset[secureRandom.nextInt(charset.size)] }
      .joinToString("")
  }
}
