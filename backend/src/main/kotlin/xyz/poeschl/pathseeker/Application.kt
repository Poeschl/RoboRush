package xyz.poeschl.pathseeker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import xyz.poeschl.pathseeker.gamelogic.GameLoop

@SpringBootApplication
@EnableScheduling
class Application(private val gameLoop: GameLoop)

fun main(args: Array<String>) {
  runApplication<Application>(*args)
}
