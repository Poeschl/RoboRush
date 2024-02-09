package xyz.poeschl.pathseeker.security

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class PasswordEncoderContainerTest {

  companion object {
    private val LOGGER = LoggerFactory.getLogger(PasswordEncoderContainerTest::class.java)
  }

  private val passwordEncoder = PasswordEncoderContainer().passwordEncoder()

  @Test
  fun createDummyPass() {
    // WHEN
    val dummyPassword = "12345678"

    // THEN
    val pass = passwordEncoder.encode(dummyPassword)

    // VERIFY
    LOGGER.info("Dummy Robot Password: {}", pass)
  }
}
