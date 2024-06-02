package xyz.poeschl.roborush.service

import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import xyz.poeschl.roborush.models.ActiveRobot
import xyz.poeschl.roborush.models.ScoreboardEntry
import xyz.poeschl.roborush.repositories.PlayedGame
import xyz.poeschl.roborush.repositories.PlayedGamesRepository
import xyz.poeschl.roborush.repositories.RobotRepository
import java.time.ZonedDateTime

@Service
class PlayedGamesService(private val playedGamesRepository: PlayedGamesRepository, private val robotRepository: RobotRepository) {

  @Cacheable("scoreBoard")
  fun getGameScoreBoard(): List<ScoreboardEntry> {
    // TODO: Make this configurable
    val until = ZonedDateTime.now().minusHours(24)
    return playedGamesRepository.getPlayedGamesUntil(until)
      .filter { it.winnerRobot != null }
      .groupBy(PlayedGame::winnerRobot)
      .map { ScoreboardEntry(it.key!!.user.username, it.key!!.color, it.value.size) }
  }

  @CacheEvict("scoreBoard", allEntries = true)
  fun insertPlayedGame(activeRobot: ActiveRobot?, currentTurn: Int) {
    val robot = if (activeRobot != null) {
      robotRepository.findById(activeRobot.id).get()
    } else {
      null
    }
    playedGamesRepository.save(PlayedGame(null, robot, currentTurn))
  }
}
