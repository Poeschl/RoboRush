package xyz.poeschl.roborush.security.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.security.core.authority.SimpleGrantedAuthority
import xyz.poeschl.roborush.test.utils.builder.Builders.Companion.a
import xyz.poeschl.roborush.test.utils.builder.NativeTypes.Companion.`$String`

class UserTest {

  @Test
  fun userAuthorities_regularUser() {
    // THEN
    val user = User(a(`$String`("user")), a(`$String`("password")))

    // VERIFY
    assertThat(user.authorities).containsExactly(SimpleGrantedAuthority("ROLE_USER"))
  }

  @Test
  fun userAuthorities_adminUser() {
    // THEN
    val user = User("root", a(`$String`("password")))

    // VERIFY
    assertThat(user.authorities).containsExactly(SimpleGrantedAuthority("ROLE_ADMIN"))
  }
}
