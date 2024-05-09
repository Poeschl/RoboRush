package xyz.poeschl.roborush.models

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ColorConverterTest {

  private val colorConverter = ColorConverter()

  @Test
  fun convertToDatabaseColumn() {
    // WHEN
    val input = Color(73, 11, 255)

    // THEN
    val result = colorConverter.convertToDatabaseColumn(input)

    // VERIFY
    assertThat(result).isEqualTo("73|11|255")
  }

  @Test
  fun convertToEntityAttribute() {
    // WHEN
    val input = "64|132|34"

    // THEN
    val result = colorConverter.convertToEntityAttribute(input)

    // VERIFY
    assertThat(result).isEqualTo(Color(64, 132, 34))
  }
}
