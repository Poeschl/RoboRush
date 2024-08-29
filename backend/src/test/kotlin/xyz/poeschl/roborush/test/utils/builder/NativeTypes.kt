package xyz.poeschl.roborush.test.utils.builder

import org.junit.jupiter.api.Disabled
import xyz.poeschl.roborush.configuration.Builder
import java.time.ZonedDateTime

@Disabled("Marking this file as a test class, to allow a relaxed linting")
class NativeTypes {
  companion object {
    fun `$Int`(): Builder<Int> = Builder { kotlin.random.Random.nextInt() }
    fun `$Int`(from: Int, until: Int): Builder<Int> = Builder { kotlin.random.Random.nextInt(from, until) }

    fun `$Long`(): Builder<Long> = Builder { kotlin.random.Random.nextLong() }

    fun `$Double`(): Builder<Double> = Builder { kotlin.random.Random.nextDouble() }
    fun `$Double`(from: Double, until: Double): Builder<Double> = Builder { kotlin.random.Random.nextDouble(from, until) }

    fun `$String`(prefix: String = ""): Builder<String> = Builder { prefix + String(kotlin.random.Random.nextBytes(10)) }

    fun `$Boolean`(): Builder<Boolean> = Builder { kotlin.random.Random.nextBoolean() }

    fun `$Id`(): Builder<Long> = `$Long`()

    fun `$Now`(): TimeBuilder<ZonedDateTime> = TimeBuilder { ZonedDateTime.now() }
  }
}
