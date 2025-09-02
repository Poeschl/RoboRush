package xyz.poeschl.roborush.security

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import xyz.poeschl.roborush.security.filter.JwtTokenFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@SecurityScheme(
  name = "Bearer Authentication",
  type = SecuritySchemeType.HTTP,
  bearerFormat = "JWT",
  scheme = "bearer"
)
class HttpSecurityConfig(private val jwtTokenFilter: JwtTokenFilter, private val authenticationConfiguration: AuthenticationConfiguration) {
  @Bean
  fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
    http.invoke {
      authorizeHttpRequests {
        authorize("/swagger-ui/**", permitAll)
        authorize("/v3/api-docs/**", permitAll)
        authorize("/ping", permitAll)
        authorize("/auth/**", permitAll)
        // Allow the error handler to work, also REST
        authorize("/error", permitAll)
        authorize("/config/client", permitAll)

        // allow the map api and websockets for all users
        authorize("/ws", permitAll)
        authorize("/robot/all/**", permitAll)
        authorize("/game", permitAll)
        authorize("/game/map", permitAll)

        authorize(anyRequest, authenticated)
      }
      sessionManagement {
        sessionCreationPolicy = SessionCreationPolicy.STATELESS
      }
      csrf {
        disable()
      }
      cors {
        disable()
      }
      addFilterBefore<UsernamePasswordAuthenticationFilter>(jwtTokenFilter)
    }

    return http.build()
  }

  @Bean
  fun authenticationManager(): AuthenticationManager = authenticationConfiguration.authenticationManager
}
