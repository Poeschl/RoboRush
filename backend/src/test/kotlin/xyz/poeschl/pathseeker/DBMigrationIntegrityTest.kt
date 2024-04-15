package xyz.poeschl.pathseeker

import de.pixel.mcc.HashAlgorithm
import de.pixel.mcc.MigrationChangeChecker
import org.junit.jupiter.params.ParameterizedTest
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
    private fun filesProvider(): Stream<Path> {
      val sqlScriptsFolder: Path = Paths.get(MIGRATION_SCRIPTS_URL)
      return Files.walk(sqlScriptsFolder)
        .filter { file -> Files.isRegularFile(file) }
        .sorted(Comparator.comparing(Path::getFileName))
    }
  }

  private val changeChecker = MigrationChangeChecker.setup()
    .withHashAlgorithm(HashAlgorithm.MD5)
    .withHashPair("V1_000__create_user_schemas.sql", "5982877bb3946da9beea4e57f4d1f57e")
    .withHashPair("V1_001__create_robot_schemas.sql", "6b37f4a87fa7ff214b3f61b997cc83c4")
    .withHashPair("V1_002__create_config_schemas.sql", "203afe532609f8e02f419b50d1313892")
    .withHashPair("V1_003__create_map_schemas.sql", "434f7ab6fc92277d6d948fadbb9c6b4c")
    .withHashPair("V1_004__add_map_icon_map.sql", "9f806a25b05e0855e157d6c4e0e58158")
    .withHashPair("V1_005__add_map_easy.sql", "554e01736174a4bee26234d472e82a7e")
    .withHashPair("V1_006__add_map_climb-up.sql", "088d2ae5394f6bde95ddba809f14a059")

  @ParameterizedTest
  @MethodSource("filesProvider")
  fun checkMigrations(file: Path?) {
    changeChecker.verifyFile(file)
  }
}
