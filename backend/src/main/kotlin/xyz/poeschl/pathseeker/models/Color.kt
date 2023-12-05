package xyz.poeschl.pathseeker.models

import kotlin.random.Random

data class Color(val r: Int, val g: Int, val b: Int) {
  companion object {
    fun randomColor(): Color = Color(Random.nextInt(255), Random.nextInt(255), Random.nextInt(255))
  }
}
