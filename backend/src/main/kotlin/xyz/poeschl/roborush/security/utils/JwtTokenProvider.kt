package xyz.poeschl.roborush.security.utils

import io.jsonwebtoken.*
import io.jsonwebtoken.security.SecurityException
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.core.token.Sha512DigestUtils
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import xyz.poeschl.roborush.security.repository.User
import java.time.ZonedDateTime
import java.util.*
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Component
class JwtTokenProvider {

  companion object {
    private val LOGGER = LoggerFactory.getLogger(JwtTokenProvider::class.java)
    private const val ROLE_CLAIM_NAME = "role"
  }

  @Value("\${AUTH_ISSUER:RoboRush}")
  private val jwtIssuer = "RoboRush"

  @Value("\${AUTH_KEY:RoboRush}")
  private val jwtSecretSource = "RoboRush"

  private lateinit var key: SecretKey

  init {
    val decodedKey = Sha512DigestUtils.sha(jwtSecretSource)
    key = SecretKeySpec(decodedKey, 0, decodedKey.size, "HmacSHA512")
  }

  // Without expire right now!
  fun createToken(authentication: Authentication): String {
    val now = ZonedDateTime.now()
    val user = authentication.principal as User
    return Jwts.builder()
      .subject(user.username)
      .claim(ROLE_CLAIM_NAME, user.authorities.first().authority)
      .issuedAt(Date.from(now.toInstant()))
      .issuer(jwtIssuer)
      .signWith(key)
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
      val jws = Jwts.parser()
        .verifyWith(key)
        .requireIssuer(jwtIssuer)
        .build()
        .parseSignedClaims(token)

      return true
    } catch (ex: IncorrectClaimException) {
      LOGGER.error("Invalid Issuer", ex)
    } catch (ex: MissingClaimException) {
      LOGGER.error("Not all required claims were found", ex)
    } catch (ex: MalformedJwtException) {
      LOGGER.error("Invalid JWT token")
    } catch (ex: ExpiredJwtException) {
      LOGGER.error("Expired JWT token")
    } catch (ex: UnsupportedJwtException) {
      LOGGER.error("Unsupported JWT token")
    } catch (ex: IllegalArgumentException) {
      LOGGER.error("JWT claims string is empty")
    } catch (e: SecurityException) {
      LOGGER.error("There is an error with the signature of you token ")
    }
    return false
  }

  fun getUsername(token: String?): String {
    // Extract the username from the JWT token
    return Jwts.parser()
      .verifyWith(key).build()
      .parseSignedClaims(token)
      .payload
      .subject
  }
}
