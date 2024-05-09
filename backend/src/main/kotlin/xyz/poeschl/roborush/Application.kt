package xyz.poeschl.roborush

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import xyz.poeschl.roborush.gamelogic.GameLoop

@SpringBootApplication
@EnableScheduling
class Application(private val gameLoop: GameLoop)

fun main(args: Array<String>) {
  runApplication<Application>(*args)
}
