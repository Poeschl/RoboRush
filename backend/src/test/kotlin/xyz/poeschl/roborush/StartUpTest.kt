package xyz.poeschl.roborush

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import xyz.poeschl.roborush.test.utils.AbstractSpringBootTest

class StartUpTest : AbstractSpringBootTest() {

  @Test
  fun canStart() {
    // This test does nothing, except starting the spring boot context once.
    // With that, we ensure that the application start up correctly.
    assertThat(true).isTrue()
  }
}
