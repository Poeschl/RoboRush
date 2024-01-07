package xyz.poeschl.pathseeker.security.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import xyz.poeschl.pathseeker.security.repository.User
import xyz.poeschl.pathseeker.security.repository.UserRepository
import xyz.poeschl.pathseeker.service.RobotService
import java.time.ZonedDateTime

class UserDetailsServiceTest {

  private val userRepository = mock<UserRepository>()
  private val robotService = mock<RobotService>()
  private val passwordEncoder = mock<PasswordEncoder>()

  private val userDetailsService = UserDetailsService(userRepository, robotService, passwordEncoder)

  @Test
  fun loadUserByUsername() {
    // WHEN
    val user = User("test", "")
    val userName = user.username

    `when`(userRepository.findByUsername(userName)).thenReturn(user)

    // THEN
    val result = userDetailsService.loadUserByUsername(userName)

    // VERIFY
    assertThat(result).isEqualTo(user)
  }

  @Test
  fun loadUserByUsername_notFound() {
    // WHEN
    val user = User("test", "")
    val userName = user.username

    `when`(userRepository.findByUsername(userName)).thenReturn(null)

    // THEN
    assertThrows<UsernameNotFoundException> {
      userDetailsService.loadUserByUsername(userName)
    }

    // VERIFY
  }

  @Test
  fun registerNewUser() {
    // WHEN
    val userName = "Bobby"
    val password = "1234"
    val encodedPassword = "42"
    val user = User(null, userName, encodedPassword)
    val savedUser = User(1L, "${userName}Saved", encodedPassword)

    `when`(passwordEncoder.encode(password)).thenReturn(encodedPassword)
    `when`(userRepository.save(user)).thenReturn(savedUser)

    // THEN
    userDetailsService.registerNewUser(userName, password)

    // VERIFY
    val userCaptor = ArgumentCaptor.forClass(User::class.java)
    verify(userRepository).save(userCaptor.capture())

    assertThat(userCaptor.value.username).isEqualTo(userName)
    assertThat(userCaptor.value.password).isEqualTo(encodedPassword)
    assertThat(userCaptor.value.registeredAt).isAfterOrEqualTo(ZonedDateTime.now().minusSeconds(5))
    assertThat(userCaptor.value.authorities).containsExactly(SimpleGrantedAuthority("user"))
    assertThat(userCaptor.value.isEnabled).isTrue()
    assertThat(userCaptor.value.isAccountNonLocked).isTrue()
    assertThat(userCaptor.value.isAccountNonExpired).isTrue()
    assertThat(userCaptor.value.isCredentialsNonExpired).isTrue()

    verify(robotService).createRobot(savedUser)
  }
}
