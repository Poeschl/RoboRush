package xyz.poeschl.pathseeker.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import xyz.poeschl.pathseeker.security.utils.JwtTokenProvider

@Component
class JwtTokenFilter(private val jwtTokenProvider: JwtTokenProvider, private val userDetailsService: UserDetailsService) : OncePerRequestFilter() {

  override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
    // Get the JWT token from the Authorization header
    val token = jwtTokenProvider.resolveToken(request)

    // Check if the token is valid
    if (token != null && jwtTokenProvider.validateToken(token)) {
      // Get the user details from the token
      val userDetails = userDetailsService.loadUserByUsername(
        jwtTokenProvider.getUsername(token)
      )
      // Create an authentication object and set it in the security context
      val authentication = UsernamePasswordAuthenticationToken(
        userDetails,
        null,
        userDetails.authorities
      )
      authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
      SecurityContextHolder.getContext().authentication = authentication
    }
    filterChain.doFilter(request, response)
  }
}
