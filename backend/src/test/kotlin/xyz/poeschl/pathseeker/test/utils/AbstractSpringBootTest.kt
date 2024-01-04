package xyz.poeschl.pathseeker.test.utils

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

/***
 * This abstract class can be inherited to start up the complete Spring Boot Context.
 * This includes also an in-memory database with test-containers and all migrations ready.
 */
@Disabled("Abstract class")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = ["xyz.poeschl.pathseeker"])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@ExtendWith(SpringExtension::class)
@AutoConfigureTestEntityManager
abstract class AbstractSpringBootTest {

  /***
   * A test entity manager is supplied for easy database manipulation.
   */
  @Autowired
  protected var entityManager: TestEntityManager? = null
}
