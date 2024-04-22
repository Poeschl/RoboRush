package xyz.poeschl.pathseeker.models

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import xyz.poeschl.pathseeker.test.utils.builder.Builders.Companion.a
import xyz.poeschl.pathseeker.test.utils.builder.GameLogicBuilder.Companion.`$Position`

class PositionConverterTest {

  private val positionConverter = PositionConverter()

  @Test
  fun convertToDatabaseColumn() {
    // WHEN
    val position = a(`$Position`())

    // THEN
    val result = positionConverter.convertToDatabaseColumn(position)

    // VERIFY
    assertThat(result).isEqualTo("${position.x},${position.y}")
  }

  @Test
  fun convertToEntityAttribute() {
    // WHEN
    val positionString = "42,24"

    // THEN
    val result = positionConverter.convertToEntityAttribute(positionString)

    // VERIFY
    assertThat(result).isEqualTo(Position(42, 24))
  }
}
