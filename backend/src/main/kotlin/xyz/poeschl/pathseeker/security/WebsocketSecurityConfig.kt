package xyz.poeschl.pathseeker.security

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.SimpMessageType
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer

// @EnableWebSocketSecurity // This new annotation is not used, since disabling the CSRF mechanism is not possible with it now.
// See https://docs.spring.io/spring-security/reference/servlet/integrations/websocket.html#websocket-sameorigin-disable
// As workaround the depreciated AbstractSecurityWebSocketMessageBrokerConfigurer is used
@Configuration
class WebsocketSecurityConfig : AbstractSecurityWebSocketMessageBrokerConfigurer() {

  override fun configureInbound(messages: MessageSecurityMetadataSourceRegistry) {
    super.configureInbound(messages)
    messages
      .simpSubscribeDestMatchers("/topic/**").permitAll()
      .simpSubscribeDestMatchers("/user/**").authenticated()
      .nullDestMatcher().permitAll() // Allow all message types which don't transfer data for everyone
      .simpTypeMatchers(SimpMessageType.SUBSCRIBE).permitAll()
      .simpTypeMatchers(SimpMessageType.MESSAGE).authenticated() // Requires authentication for sending messages
      .anyMessage().denyAll() // Falls back to denial
  }

  override fun sameOriginDisabled() = true
}
