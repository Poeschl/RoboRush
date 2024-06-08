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
    .withHashPair("V1_001__create_robot_schemas.sql", "6b37f4a87fa7ff214b3f61b997cc83c4")
    .withHashPair("V1_002__create_config_schemas.sql", "255b974ce89a346997a26064f1efecc8")
    .withHashPair("V1_003__create_map_schemas.sql", "69f4ddc8bd39e42d586afca833e4e6f8")
    .withHashPair("V1_004__add_map_icon_map.sql", "018fc6ef7adba9b861d33394bbcc57c2")
    .withHashPair("V1_005__add_map_easy.sql", "c0a99225ea3ac682bd576590d630d94b")
    .withHashPair("V1_006__add_map_climb-up.sql", "6b0ef0f057f6d712c5f63e9733a2974e")

  @ParameterizedTest(name = "{1}")
  @MethodSource("filesProvider")
  fun checkMigrations(file: Path?, fileName: String) {
    changeChecker.verifyFile(file)
  }
}
