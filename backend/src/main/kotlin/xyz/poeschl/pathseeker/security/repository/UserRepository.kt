package xyz.poeschl.pathseeker.security.repository

import jakarta.persistence.*
import net.karneim.pojobuilder.GeneratePojoBuilder
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.repository.CrudRepository
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Repository
import xyz.poeschl.pathseeker.configuration.Builder
import java.time.ZonedDateTime

@Repository
interface UserRepository : CrudRepository<User, Long> {

  fun findByUsername(username: String): User?
}

@Entity
@Table(name = "users")
data class User
@GeneratePojoBuilder(withBuilderInterface = Builder::class)
constructor(
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(insertable = false) private val id: Long?,
  // username and password are private due to a getter clash with the interface methods
  @Column(name = "username", unique = true) private var username: String,
  @Column(name = "password") private var password: String,
  @CreatedDate @Column(name = "registered_at") var registeredAt: ZonedDateTime = ZonedDateTime.now()
) : UserDetails {

  companion object {
    const val ROLE_ADMIN = "ADMIN"
    const val ROLE_USER = "USER"
    const val ROOT_USERNAME = "root"
  }

  constructor(username: String, password: String) : this(null, username, password)

  override fun getUsername() = username

  override fun getPassword() = password

  override fun getAuthorities(): Collection<GrantedAuthority> =
    if (username == ROOT_USERNAME) listOf(SimpleGrantedAuthority(ROLE_ADMIN)) else listOf(SimpleGrantedAuthority(ROLE_USER))

  override fun isAccountNonExpired() = true

  override fun isAccountNonLocked() = true

  override fun isCredentialsNonExpired() = true

  override fun isEnabled() = true
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as User

    if (id != other.id) return false
    if (username != other.username) return false
    if (password != other.password) return false

    return true
  }

  override fun hashCode(): Int {
    var result = id?.hashCode() ?: 0
    result = 31 * result + username.hashCode()
    result = 31 * result + password.hashCode()
    return result
  }
}
