package xyz.poeschl.pathseeker.test.utils.builder

import org.junit.jupiter.api.Disabled
import xyz.poeschl.pathseeker.security.repository.UserBuilder
import xyz.poeschl.pathseeker.test.utils.builder.Builders.Companion.a
import xyz.poeschl.pathseeker.test.utils.builder.Builders.Companion.time
import xyz.poeschl.pathseeker.test.utils.builder.NativeTypes.Companion.`$Id`
import xyz.poeschl.pathseeker.test.utils.builder.NativeTypes.Companion.`$Now`
import xyz.poeschl.pathseeker.test.utils.builder.NativeTypes.Companion.`$String`

@Disabled("Marking this file as a test class, to allow a relaxed linting")
class SecurityBuilder {

  companion object {
    fun `$User`(): UserBuilder = xyz.poeschl.pathseeker.security.repository.UserBuilder()
      .withId(a(`$Id`()))
      .withUsername(a(`$String`()))
      .withPassword(a(`$String`()))
      .withRegisteredAt(time(`$Now`()))
  }
}
