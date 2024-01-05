package xyz.poeschl.pathseeker.security

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import xyz.poeschl.pathseeker.repositories.User
import xyz.poeschl.pathseeker.repositories.UserRepository

@Configuration
class UserDetailsService(
  private val userRepository: UserRepository,
  private val passwordEncoder: PasswordEncoder
) : UserDetailsService {

  companion object {
    private val LOGGER = LoggerFactory.getLogger(UserDetailsService::class.java)
  }

  override fun loadUserByUsername(username: String): UserDetails {
    return userRepository.findByUsername(username) ?: throw UsernameNotFoundException("User '$username' not found!")
  }

  fun checkUsernameExists(username: String): Boolean {
    return userRepository.findByUsername(username) != null
  }

  fun registerNewUser(username: String, password: String) {
    val encodedPassword = passwordEncoder.encode(password)
    userRepository.save(User(username, encodedPassword))
  }
}
