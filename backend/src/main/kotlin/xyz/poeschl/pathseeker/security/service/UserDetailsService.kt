package xyz.poeschl.pathseeker.security.service

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import xyz.poeschl.pathseeker.security.repository.User
import xyz.poeschl.pathseeker.security.repository.UserRepository
import xyz.poeschl.pathseeker.service.RobotService

@Configuration
class UserDetailsService(
  private val userRepository: UserRepository,
  private val robotService: RobotService,
  private val passwordEncoder: PasswordEncoder
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
    robotService.createRobot(user)
  }
}
