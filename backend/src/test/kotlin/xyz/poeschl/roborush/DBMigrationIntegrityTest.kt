package xyz.poeschl.roborush

import de.pixel.mcc.HashAlgorithm
import de.pixel.mcc.MigrationChangeChecker
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
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
    .withHashPair("V1_001__create_robot_schemas.sql", "1cf42c0d26713eb7b6d42894118b82e1")
    .withHashPair("V1_002__create_config_schemas.sql", "e1d94480bbcd84b669c7d8a68fd8ef63")
    .withHashPair("V1_003__create_map_schemas.sql", "1241f041234a0e4b6a62e9f0b2b4ba20")
    .withHashPair("V1_004__add_map_icon_map.sql", "2aa3f876a708aaf32eb95b8f6cec980a")
    .withHashPair("V1_005__add_map_easy.sql", "66975341559c243797bcd6871b0a49dd")
    .withHashPair("V1_006__add_map_climb-up.sql", "b0e362ce9f195b2b3ab1cfc8aa4e44e3")
    .withHashPair("V1_007__add_additional_settings.sql", "03a7108b0313503e3986b88afb917113")

  @ParameterizedTest(name = "{1}")
  @MethodSource("filesProvider")
  fun checkMigrations(file: Path?, fileName: String) {
    changeChecker.verifyFile(file)
  }
}
