package xyz.poeschl.pathseeker.models

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import kotlin.random.Random

data class Color(val r: Int, val g: Int, val b: Int) {
  companion object {
    fun randomColor(): Color = Color(Random.nextInt(255), Random.nextInt(255), Random.nextInt(255))
  }
}

@Converter(autoApply = true)
class ColorConverter : AttributeConverter<Color, String> {
  override fun convertToDatabaseColumn(attribute: Color): String {
    return "${attribute.r}|${attribute.g}|${attribute.b}"
  }

  override fun convertToEntityAttribute(dbData: String): Color {
    val split = dbData.split("|").map { it.toInt() }
    return Color(split[0], split[1], split[2])
  }
}
