package xyz.poeschl.roborush.models

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import net.karneim.pojobuilder.GeneratePojoBuilder
import xyz.poeschl.roborush.configuration.Builder
import kotlin.random.Random

@GeneratePojoBuilder(withBuilderInterface = Builder::class)
data class Color(val r: Int, val g: Int, val b: Int) {
  companion object {
    fun randomColor(): Color = Color(Random.nextInt(255), Random.nextInt(255), Random.nextInt(255))
    fun fromColorInt(color: Int): Color = Color((color and 0xff0000) shr 16, (color and 0xff00) shr 8, color and 0xff)
  }

  // Allow a little pixel color delta
  @JsonIgnore
  fun isGrey() = (r == g || r == g - 1 || r == g + 1) && (g == b || g == b - 1 || g == b + 1) && (b == r || b == r - 1 || b == r + 1)

  @JsonIgnore
  fun toAwtColor(): java.awt.Color = java.awt.Color(this.r, this.g, this.b)
}

@Converter(autoApply = true)
class ColorConverter : AttributeConverter<Color, String> {
  override fun convertToDatabaseColumn(attribute: Color): String = "${attribute.r}|${attribute.g}|${attribute.b}"

  override fun convertToEntityAttribute(dbData: String): Color {
    val split = dbData.split("|").map { it.toInt() }
    return Color(split[0], split[1], split[2])
  }
}
