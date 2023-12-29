package xyz.poeschl.pathseeker.configuration

import org.springdoc.core.customizers.OpenApiCustomizer
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class OpenApiConfig {

  @Bean
  fun robotOpenApi(): GroupedOpenApi = GroupedOpenApi.builder().group("public").addOpenApiCustomizer(filterForPublicTag()).build()

  @Bean
  @Profile("!prod")
  fun internalOpenApi(): GroupedOpenApi = GroupedOpenApi.builder().group("internal").addOpenApiCustomizer(filterForNonePublicTag()).build()

  private fun filterForPublicTag(): OpenApiCustomizer = OpenApiCustomizer { openApi ->
    openApi.paths.entries
      .removeIf { path ->
        path.value.readOperations().stream().noneMatch { operation -> operation.tags.contains("public") }
      }
  }

  private fun filterForNonePublicTag(): OpenApiCustomizer = OpenApiCustomizer { openApi ->
    openApi.paths.entries
      .removeIf { path ->
        path.value.readOperations().stream().anyMatch { operation -> operation.tags.contains("public") }
      }
  }
}
