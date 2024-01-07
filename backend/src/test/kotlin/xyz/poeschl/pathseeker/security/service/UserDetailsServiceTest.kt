package xyz.poeschl.pathseeker.security.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import xyz.poeschl.pathseeker.security.repository.User
import xyz.poeschl.pathseeker.security.repository.UserRepository
import xyz.poeschl.pathseeker.service.RobotService
import xyz.poeschl.pathseeker.test.utils.builder.Builders.Companion.a
import xyz.poeschl.pathseeker.test.utils.builder.SecurityBuilder.Companion.`$User`
import java.time.ZonedDateTime

class UserDetailsServiceTest {

  private val userRepository = mockk<UserRepository>()
  private val robotService = mockk<RobotService>()
  private val passwordEncoder = mockk<PasswordEncoder>()

  private val userDetailsService = UserDetailsService(userRepository, robotService, passwordEncoder)

  @Test
  fun loadUserByUsername() {
    // WHEN
    val user = a(`$User`())
    val userName = user.username

    every { userRepository.findByUsername(userName) } returns user

    // THEN
    val result = userDetailsService.loadUserByUsername(userName)

    // VERIFY
    assertThat(result).isEqualTo(user)
  }

  @Test
  fun loadUserByUsername_notFound() {
    // WHEN
    val user = a(`$User`())
    val userName = user.username

    every { userRepository.findByUsername(userName) } returns null

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
    val user = a(`$User`().withUsername(userName).withPassword(encodedPassword).withId(null))
    val savedUser = a(`$User`().withUsername(userName).withPassword(encodedPassword).withId(1))

    every { passwordEncoder.encode(password) } returns encodedPassword
    every { userRepository.save(user) } returns savedUser
    every { robotService.createRobot(savedUser) } returns mockk()

    // THEN
    userDetailsService.registerNewUser(userName, password)

    // VERIFY
    val userSlot = slot<User>()
    verify { userRepository.save(capture(userSlot)) }

    assertThat(userSlot.captured.username).isEqualTo(userName)
    assertThat(userSlot.captured.password).isEqualTo(encodedPassword)
    assertThat(userSlot.captured.registeredAt).isAfterOrEqualTo(ZonedDateTime.now().minusSeconds(5))
    assertThat(userSlot.captured.authorities).containsExactly(SimpleGrantedAuthority("user"))
    assertThat(userSlot.captured.isEnabled).isTrue()
    assertThat(userSlot.captured.isAccountNonLocked).isTrue()
    assertThat(userSlot.captured.isAccountNonExpired).isTrue()
    assertThat(userSlot.captured.isCredentialsNonExpired).isTrue()

    verify { robotService.createRobot(savedUser) }
  }
}
