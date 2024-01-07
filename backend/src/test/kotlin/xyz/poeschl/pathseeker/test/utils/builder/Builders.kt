package xyz.poeschl.pathseeker.test.utils.builder

import org.junit.jupiter.api.Disabled
import xyz.poeschl.pathseeker.configuration.Builder

@Disabled("Marking this file as a test class, to allow a relaxed linting")
class Builders {
  companion object {

    /***
     * This builds an instance of the given builder
     * @param builder A builder (prefixed with '$'
     * @return A created instance
     */
    fun <T> a(builder: Builder<T>): T {
      return builder.build()
    }

    /***
     * This builds a single list with one element in it, the created instance.
     * @param builder A builder (prefixed with '$'
     * @return A list with one element and the built instance in it.
     */
    fun <T> listWithOne(builder: Builder<T>): List<T> {
      return listOf(builder.build())
    }

    /***
     * This builds a single set with one element in it, the created instance.
     * @param builder A builder (prefixed with '$'
     * @return A set with one element and the built instance in it.
     */
    fun <T> setWithOne(builder: Builder<T>): Set<T> {
      return setOf(builder.build())
    }

    /**
     * This returns a time-related instance.
     * @param builder The builder which determine the time.
     * @return A instance of this time
     */
    fun <T> time(builder: TimeBuilder<T>): T {
      return builder.build()
    }
  }
}

fun interface TimeBuilder<T> : Builder<T>
