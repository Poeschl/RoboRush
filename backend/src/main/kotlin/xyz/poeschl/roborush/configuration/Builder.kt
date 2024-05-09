package xyz.poeschl.roborush.configuration

fun interface Builder<T> {
  fun build(): T
}
