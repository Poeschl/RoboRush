package xyz.poeschl.pathseeker

import de.pixel.mcc.HashAlgorithm
import de.pixel.mcc.MigrationChangeChecker
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Comparator
import java.util.stream.Stream

class DBMigrationIntegrityTest {

  companion object {
    private const val MIGRATION_SCRIPTS_URL: String = "src/main/resources/db/migration"

    @JvmStatic
    private fun filesProvider(): Stream<Arguments> {
      val sqlScriptsFolder: Path = Paths.get(MIGRATION_SCRIPTS_URL)
      return Files.walk(sqlScriptsFolder)
        .filter { file -> Files.isRegularFile(file) }
        .sorted(Comparator.comparing(Path::getFileName))
        .map { Arguments.of(it, it.fileName.toString()) }
    }
  }

  private val changeChecker = MigrationChangeChecker.setup()
    .withHashAlgorithm(HashAlgorithm.MD5)
    .withHashPair("V1_000__create_user_schemas.sql", "5982877bb3946da9beea4e57f4d1f57e")
    .withHashPair("V1_001__create_robot_schemas.sql", "6b37f4a87fa7ff214b3f61b997cc83c4")
    .withHashPair("V1_002__create_config_schemas.sql", "536f4e48da2794ab9df48f2bb9670da3")
    .withHashPair("V1_003__create_map_schemas.sql", "aafd810bf1a4b4969cc58f799ef99617")
    .withHashPair("V1_004__add_map_icon_map.sql", "b34e18c8f5cea878a5ed86e8f9236a03")
    .withHashPair("V1_005__add_map_easy.sql", "011030835cb18f982d0a364fb4ef03a2")
    .withHashPair("V1_006__add_map_climb-up.sql", "c1aaa22ccdea31a9cf972b31be3ed3ae")

  @ParameterizedTest(name = "{1}")
  @MethodSource("filesProvider")
  fun checkMigrations(file: Path?, fileName: String) {
    changeChecker.verifyFile(file)
  }
}
