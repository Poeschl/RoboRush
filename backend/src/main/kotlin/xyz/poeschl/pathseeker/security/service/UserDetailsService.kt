package xyz.poeschl.pathseeker.security.service

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import xyz.poeschl.pathseeker.models.Color
import xyz.poeschl.pathseeker.repositories.Robot
import xyz.poeschl.pathseeker.repositories.RobotRepository
import xyz.poeschl.pathseeker.security.repository.User
import xyz.poeschl.pathseeker.security.repository.UserRepository
import xyz.poeschl.pathseeker.security.utils.JwtTokenProvider

@Configuration
class UserDetailsService(
  private val userRepository: UserRepository,
  private val robotRepository: RobotRepository,
  private val passwordEncoder: PasswordEncoder,
  private val jwtTokenProvider: JwtTokenProvider
) : UserDetailsService {

  companion object {
    private val LOGGER = LoggerFactory.getLogger(UserDetailsService::class.java)
  }

  override fun loadUserByUsername(username: String): UserDetails {
    return userRepository.findByUsername(username) ?: throw UsernameNotFoundException("User '$username' not found!")
  }

  fun registerNewUser(username: String, password: String) {
    val encodedPassword = passwordEncoder.encode(password)
    val user = userRepository.save(User(username, encodedPassword))
    robotRepository.save(Robot(null, Color.randomColor(), user))
  }

  fun loadUserByToken(token: String): UserDetails? {
    return if (token.isNotBlank() && jwtTokenProvider.validateToken(token)) {
      loadUserByUsername(jwtTokenProvider.getUsername(token))
    } else {
      null
    }
  }
}
