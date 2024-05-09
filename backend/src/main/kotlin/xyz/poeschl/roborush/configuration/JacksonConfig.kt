package xyz.poeschl.roborush.configuration

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

@Configuration
class JacksonConfig {

  @Bean
  fun jsonCustomizer(): Jackson2ObjectMapperBuilderCustomizer {
    return Jackson2ObjectMapperBuilderCustomizer { builder: Jackson2ObjectMapperBuilder ->

      // Setup proper duration serialization in ISO-8601
      builder.modulesToInstall(JavaTimeModule())
      val kotlinModule = KotlinModule.Builder()
        .enable(KotlinFeature.UseJavaDurationConversion)
        .build()
      builder.modulesToInstall(kotlinModule)
    }
  }
}
