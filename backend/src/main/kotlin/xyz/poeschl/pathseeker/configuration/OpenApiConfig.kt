package xyz.poeschl.pathseeker.configuration

import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class OpenApiConfig {

  @Bean
  fun robotOpenApi(): GroupedOpenApi = GroupedOpenApi.builder().group("robot").pathsToMatch("/robot/**").build()

  @Bean
  @Profile("!prod")
  fun internalOpenApi(): GroupedOpenApi = GroupedOpenApi.builder().group("internal").pathsToExclude("/robot/**").build()
}
