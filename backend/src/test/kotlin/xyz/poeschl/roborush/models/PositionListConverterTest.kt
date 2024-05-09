package xyz.poeschl.roborush.models

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PositionListConverterTest {

  private val positionListConverter = PositionListConverter()

  @Test
  fun convertToDatabaseColumn() {
    // WHEN
    val position1 = Position(3, 4)
    val position2 = Position(8, 9)

    // THEN
    val result = positionListConverter.convertToDatabaseColumn(listOf(position1, position2))

    // VERIFY
    assertThat(result).isEqualTo("${position1.x},${position1.y}|${position2.x},${position2.y}")
  }

  @Test
  fun convertToEntityAttribute() {
    // WHEN
    val positionListString = "42,24|11,33"

    // THEN
    val result = positionListConverter.convertToEntityAttribute(positionListString)

    // VERIFY
    assertThat(result).containsOnly(Position(42, 24), Position(11, 33))
  }
}
