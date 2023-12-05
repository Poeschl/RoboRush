package xyz.poeschl.pathseeker.repositories.entities

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.repository.CrudRepository
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Repository
import java.time.ZonedDateTime

@Repository
interface UserRepository : CrudRepository<User, Long> {

  fun findByUsername(username: String): User?
}

@Entity
@Table(name = "users")
data class User(
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(insertable = false) private val id: Long?,
  // username and password are private due to a getter clash with the interface methods
  @Column(name = "username", unique = true) private var username: String,
  @Column(name = "password") private var password: String,
  @CreatedDate @Column(name = "registered_at") var registeredAt: ZonedDateTime = ZonedDateTime.now()
) : UserDetails {

  constructor(username: String, password: String) : this(null, username, password)
  override fun getUsername() = username

  override fun getPassword() = password

  override fun getAuthorities(): Collection<GrantedAuthority> = listOf(SimpleGrantedAuthority("user"))

  override fun isAccountNonExpired() = true

  override fun isAccountNonLocked() = true

  override fun isCredentialsNonExpired() = true

  override fun isEnabled() = true
}
