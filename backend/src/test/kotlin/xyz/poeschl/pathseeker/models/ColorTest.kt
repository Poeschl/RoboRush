package xyz.poeschl.pathseeker.models

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class ColorTest {

  companion object {
    @JvmStatic
    fun colorGrayTestData() = Stream.of(
      Arguments.of(Color(100, 100, 100), true),
      Arguments.of(Color(101, 100, 100), true),
      Arguments.of(Color(100, 101, 100), true),
      Arguments.of(Color(100, 100, 101), true),
      Arguments.of(Color(99, 100, 100), true),
      Arguments.of(Color(100, 99, 100), true),
      Arguments.of(Color(100, 100, 99), true),
      Arguments.of(Color(101, 100, 99), false),
      Arguments.of(Color(99, 101, 100), false),
      Arguments.of(Color(99, 100, 101), false)
    )
  }

  @Test
  fun fromColorInt() {
    // WHEN
    val intColor = 819132

    // THEN
    val color = Color.fromColorInt(intColor)

    // VERIFY
    assertThat(color).isEqualTo(Color(12, 127, 188))
  }

  @Test
  fun isGrey() {
    // WHEN
    val grey = Color(100, 100, 100)

    // THEN
    val isGrey = grey.isGrey()

    // VERIFY
    assertThat(isGrey).isTrue()
  }

  @Test
  fun isGrey_not() {
    // WHEN
    val grey = Color(98, 219, 100)

    // THEN
    val isGrey = grey.isGrey()

    // VERIFY
    assertThat(isGrey).isFalse()
  }

  @ParameterizedTest
  @MethodSource("colorGrayTestData")
  fun isGrey_withTolerance(color: Color, shouldBeGrey: Boolean) {
    // WHEN

    // THEN
    val greyResult = color.isGrey()

    // VERIFY
    assertThat(greyResult).isEqualTo(shouldBeGrey)
  }
}
