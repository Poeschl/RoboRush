package xyz.poeschl.roborush.service

import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import xyz.poeschl.roborush.models.ActiveRobot
import xyz.poeschl.roborush.models.ScoreboardEntry
import xyz.poeschl.roborush.repositories.PlayedGame
import xyz.poeschl.roborush.repositories.PlayedGamesRepository
import xyz.poeschl.roborush.repositories.RobotRepository
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

@Service
class PlayedGamesService(private val playedGamesRepository: PlayedGamesRepository, private val robotRepository: RobotRepository) {

  companion object {
    private val LOGGER = LoggerFactory.getLogger(PlayedGamesService::class.java)

    private val MATCH_STORE_RETENTION = Duration.ofDays(365)
  }

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
  fun insertPlayedGame(activeRobot: ActiveRobot, currentTurn: Int) {
    val robot = robotRepository.findById(activeRobot.id).get()
    playedGamesRepository.save(PlayedGame(null, robot, currentTurn))
  }

  @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
  fun deleteOldPlayedGames() {
    val cutoff = ZonedDateTime.now().minus(MATCH_STORE_RETENTION)
    val oldGames = playedGamesRepository.getPlayedGamesBefore(cutoff)
    playedGamesRepository.deleteAll(oldGames)
    if (oldGames.isNotEmpty()) {
      LOGGER.info("Deleted ${oldGames.size} played games older than $cutoff")
    }
  }
}
