package xyz.poeschl.roborush.configuration

import org.springframework.core.annotation.AliasFor
import org.springframework.stereotype.Component

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Component
annotation class GameLogic(@get:AliasFor(annotation = Component::class) val value: String = "")
