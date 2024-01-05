package xyz.poeschl.pathseeker.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class PasswordEncoderContainer {

  /**
   *  Necessary to provide the password encoder in the UserDetailsService and Security config without cyclic dependency.
   */
  @Bean
  fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
