package xyz.poeschl.pathseeker.security.filter

import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Component
import xyz.poeschl.pathseeker.security.service.UserDetailsService

/**
 * An interceptor for websocket traffic, which authenticates the user during the first CONNECT to the websocket server.
 * For that the auth JWT token is sent as header and captures here.
 * */
@Component
class WebsocketConnectAuthInterceptor(private val userDetailsService: UserDetailsService) : ChannelInterceptor {

  companion object {
    private const val AUTH_HEADER = "authentication"
  }

  override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
    val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)
    if (accessor != null && StompCommand.CONNECT == accessor.command) {
      val token = accessor.getFirstNativeHeader(AUTH_HEADER)
      if (token != null) {
        val user = userDetailsService.loadUserByToken(token)
        if (user != null) {
          accessor.setUser(UsernamePasswordAuthenticationToken(user, null, user.authorities))
        }
      }
    }
    return message
  }
}
