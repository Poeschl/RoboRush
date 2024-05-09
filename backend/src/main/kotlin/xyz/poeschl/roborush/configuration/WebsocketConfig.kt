package xyz.poeschl.roborush.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import xyz.poeschl.roborush.security.filter.WebsocketConnectAuthInterceptor

@Configuration
@EnableWebSocketMessageBroker
class WebsocketConfig(private val websocketConnectAuthInterceptor: WebsocketConnectAuthInterceptor) : WebSocketMessageBrokerConfigurer {

  override fun configureMessageBroker(registry: MessageBrokerRegistry) {
    registry.setApplicationDestinationPrefixes("/app")
    registry.enableSimpleBroker("/topic/", "/user/")
      .setTaskScheduler(heartBeatScheduler())
  }

  override fun registerStompEndpoints(registry: StompEndpointRegistry) {
    registry.addEndpoint("/ws")
  }

  override fun configureClientInboundChannel(registration: ChannelRegistration) {
    super.configureClientInboundChannel(registration)
    registration.interceptors(websocketConnectAuthInterceptor)
  }

  @Bean
  fun heartBeatScheduler(): TaskScheduler {
    return ThreadPoolTaskScheduler()
  }
}
