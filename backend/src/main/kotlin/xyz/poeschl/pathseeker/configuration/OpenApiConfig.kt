package xyz.poeschl.pathseeker.configuration

import io.swagger.v3.oas.models.Operation
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class OpenApiConfig {

  companion object {
    public const val VISIBILITY_KEY = "visibility"
    public const val VISIBILITY_PUBLIC = "public"
  }

  @Bean
  fun robotOpenApi(): GroupedOpenApi = GroupedOpenApi.builder().group("public").addOpenApiCustomizer(filterForPublicTag()).build()

  @Bean
  @Profile("!prod")
  fun internalOpenApi(): GroupedOpenApi = GroupedOpenApi.builder().group("internal").addOpenApiCustomizer(filterForNonePublicTag()).build()

  private fun filterForPublicTag(): OpenApiCustomizer = OpenApiCustomizer { openApi ->
    openApi.paths.entries
      .removeIf { path ->
        path.value.readOperations().stream().noneMatch { operation ->
          visibleForPublic(operation)
        }
      }
  }

  private fun filterForNonePublicTag(): OpenApiCustomizer = OpenApiCustomizer { openApi ->
    openApi.paths.entries
      .removeIf { path ->
        path.value.readOperations().stream().anyMatch { operation -> visibleForPublic(operation) }
      }
  }

  private fun visibleForPublic(operation: Operation): Boolean {
    return if (!operation.extensions.isNullOrEmpty() && operation.extensions.containsKey("x-$VISIBILITY_KEY")) {
      val visibility = operation.extensions["x-$VISIBILITY_KEY"] as HashMap<*, *>
      visibility[VISIBILITY_KEY] == VISIBILITY_PUBLIC
    } else {
      false
    }
  }
}
