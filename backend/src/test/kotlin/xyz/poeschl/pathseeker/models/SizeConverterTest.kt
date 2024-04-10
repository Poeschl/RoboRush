package xyz.poeschl.pathseeker.models

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import xyz.poeschl.pathseeker.test.utils.builder.Builders.Companion.a
import xyz.poeschl.pathseeker.test.utils.builder.GameLogicBuilder.Companion.`$Size`

class SizeConverterTest {

  private val sizeConverter = SizeConverter()

  @Test
  fun convertToDatabaseColumn() {
    // WHEN
    val size = a(`$Size`())

    // THEN
    val result = sizeConverter.convertToDatabaseColumn(size)

    // VERIFY
    assertThat(result).isEqualTo("${size.width}x${size.height}")
  }

  @Test
  fun convertToEntityAttribute() {
    // WHEN
    val sizeString = "42x24"

    // THEN
    val result = sizeConverter.convertToEntityAttribute(sizeString)

    // VERIFY
    assertThat(result).isEqualTo(Size(42, 24))
  }
}
