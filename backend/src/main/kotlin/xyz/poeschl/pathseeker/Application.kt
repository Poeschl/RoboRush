package xyz.poeschl.pathseeker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.EnableScheduling
import xyz.poeschl.pathseeker.gamelogic.GameLoop

@SpringBootApplication
@EnableScheduling
class Application(private val gameLoop: GameLoop) {
  @EventListener(ApplicationReadyEvent::class)
  fun afterStart() {
    gameLoop.startGame()
  }
}

fun main(args: Array<String>) {
  runApplication<Application>(*args)
}
