package xyz.poeschl.pathseeker.configuration.security.utils

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SecurityException
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import xyz.poeschl.pathseeker.repositories.User
import java.security.Key
import java.time.Duration
import java.time.ZonedDateTime
import java.util.*

@Component
class JwtTokenProvider {

  companion object {
    private val LOGGER = LoggerFactory.getLogger(JwtTokenProvider::class.java)
    private val EXPIRE_RANGE = Duration.ofDays(1)
    private const val ISSUER = "PathSeeker"
  }

  private val key: Key = Keys.secretKeyFor(SignatureAlgorithm.HS512)

  fun createToken(authentication: Authentication): String {
    val now = ZonedDateTime.now()
    val expiryDate = now.plus(EXPIRE_RANGE)
    val user = authentication.principal as User
    return Jwts.builder()
      .setSubject(user.username)
      .setIssuedAt(Date.from(now.toInstant()))
      .setExpiration(Date.from(expiryDate.toInstant()))
      .setIssuer(ISSUER)
      .signWith(key, SignatureAlgorithm.HS512)
      .compact()
  }

  fun resolveToken(request: HttpServletRequest): String? {
    val bearerToken = request.getHeader("Authorization")
    return if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      bearerToken.substring(7)
    } else {
      null
    }
  }

  fun validateToken(token: String?): Boolean {
    // Check if the token is valid and not expired
    try {
      val jws = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)

      val issuer = jws.body.issuer
      if (!issuer.equals(ISSUER)) {
        LOGGER.error("Invalid Issuer. Detected issuer: $issuer")
        return false
      }

      return true
    } catch (ex: MalformedJwtException) {
      LOGGER.error("Invalid JWT token")
    } catch (ex: ExpiredJwtException) {
      LOGGER.error("Expired JWT token")
    } catch (ex: UnsupportedJwtException) {
      LOGGER.error("Unsupported JWT token")
    } catch (ex: IllegalArgumentException) {
      LOGGER.error("JWT claims string is empty")
    } catch (e: SecurityException) {
      LOGGER.error("there is an error with the signature of you token ")
    }
    return false
  }

  fun getUsername(token: String?): String {
    // Extract the username from the JWT token
    return Jwts.parserBuilder().setSigningKey(key).build()
      .parseClaimsJws(token)
      .body
      .subject
  }
}
