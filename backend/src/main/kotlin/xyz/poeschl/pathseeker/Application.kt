package xyz.poeschl.pathseeker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.event.EventListener
import xyz.poeschl.pathseeker.service.GameService

@SpringBootApplication
class Application(private val gameService: GameService) {
  @EventListener(ApplicationReadyEvent::class)
  fun afterStart() {
    gameService.startGame()
  }
}

fun main(args: Array<String>) {
  runApplication<Application>(*args)
}
