package xyz.poeschl.pathseeker.configuration

import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

  @Bean
  fun robotOpenApi(): GroupedOpenApi = GroupedOpenApi.builder().group("robot").pathsToMatch("/robot/**").build()

  @Bean
  fun internalOpenApi(): GroupedOpenApi = GroupedOpenApi.builder().group("internal").pathsToExclude("/robot/**").build()
}
