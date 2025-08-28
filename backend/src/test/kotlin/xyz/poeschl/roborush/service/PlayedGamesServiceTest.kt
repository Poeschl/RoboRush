package xyz.poeschl.roborush.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import xyz.poeschl.roborush.gamelogic.dummybots.DummyBots
import xyz.poeschl.roborush.models.ScoreboardEntry
import xyz.poeschl.roborush.repositories.PlayedGame
import xyz.poeschl.roborush.repositories.PlayedGamesRepository
import xyz.poeschl.roborush.repositories.RobotRepository
import xyz.poeschl.roborush.test.utils.builder.Builders.Companion.a
import xyz.poeschl.roborush.test.utils.builder.GameLogicBuilder.Companion.`$ActiveRobot`
import xyz.poeschl.roborush.test.utils.builder.GameLogicBuilder.Companion.`$PlayedGame`
import xyz.poeschl.roborush.test.utils.builder.GameLogicBuilder.Companion.`$Robot`
import xyz.poeschl.roborush.test.utils.builder.NativeTypes.Companion.`$Id`
import xyz.poeschl.roborush.test.utils.builder.NativeTypes.Companion.`$Int`
import java.util.*

class PlayedGamesServiceTest {

  private val playedGamesRepository = mockk<PlayedGamesRepository>()
  private val robotRepository = mockk<RobotRepository>()
  private val dummyBots = mockk<DummyBots>()

  private val playedGamesService = PlayedGamesService(playedGamesRepository, robotRepository)

  @Test
  fun getGameScoreBoard() {
    // WHEN
    val robot1 = a(`$Robot`().withId(1L))
    val robot2 = a(`$Robot`().withId(2L))
    val game1 = a(`$PlayedGame`().withWinnerRobot(robot1))
    val game2 = a(`$PlayedGame`().withWinnerRobot(robot2))
    val game3 = a(`$PlayedGame`().withWinnerRobot(robot2))
    val game4 = a(`$PlayedGame`().withWinnerRobot(null))
    every { playedGamesRepository.getPlayedGamesUntil(any()) } returns listOf(game1, game2, game3)
    every { playedGamesRepository.getPlayedGamesUntil(any()) } returns listOf(game1, game2, game3, game4)

    // THEN
    val result = playedGamesService.getGameScoreBoard()

    // VERIFY
    assertThat(result).containsOnly(
      ScoreboardEntry(robot1.user.username, robot1.color, 1),
      ScoreboardEntry(robot2.user.username, robot2.color, 2)
    )
  }

  @Test
  fun insertPlayedGame() {
    // WHEN
    val winningRobot = a(`$ActiveRobot`().withId(a(`$Id`())))
    val currentTurn = a(`$Int`())
    val robot = a(`$Robot`().withId(a(`$Id`())))

    every { robotRepository.findById(winningRobot.id) } returns Optional.of(robot)
    every { dummyBots.isDummyRobot(robot) } returns false
    every { playedGamesRepository.save(any()) } returnsArgument 0

    // THEN
    playedGamesService.insertPlayedGame(winningRobot, currentTurn)

    // VERIFY
    val saveSlot = slot<PlayedGame>()
    verify { playedGamesRepository.save(capture(saveSlot)) }

    assertThat(saveSlot.captured.winnerRobot).isEqualTo(robot)
    assertThat(saveSlot.captured.turnsTaken).isEqualTo(currentTurn)
  }

  @Test
  fun deleteOldPlayedGames() {
    // WHEN
    val oldGames = listOf(
      a(`$PlayedGame`()),
      a(`$PlayedGame`())
    )

    every { playedGamesRepository.getPlayedGamesBefore(any()) } returns oldGames
    every { playedGamesRepository.deleteAll(any()) } returns Unit

    // THEN
    playedGamesService.deleteOldPlayedGames()

    // VERIFY
    verify { playedGamesRepository.getPlayedGamesBefore(any()) }
    verify { playedGamesRepository.deleteAll(oldGames) }
  }
}
