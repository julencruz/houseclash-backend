package com.houseclash.backend.infrastructure.persistence.user

import com.houseclash.backend.domain.model.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class UserJpaEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val username: String = "",

    @Column(unique = true, nullable = false)
    val email: String = "",

    @Column(nullable = false)
    val passwordHash: String = "",

    @Column(name = "house_id")
    val houseId: Long? = null,

    @Column(nullable = false)
    val kudosBalance: Int = 0,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

fun UserJpaEntity.toDomain(): User {
    return User(
        id = this.id,
        username = this.username,
        email = this.email,
        passwordHash = this.passwordHash,
        kudosBalance = this.kudosBalance,
        houseId = this.houseId,
        createdAt = this.createdAt
    )
}

fun User.toEntity(): UserJpaEntity {
    return UserJpaEntity(
        id = this.id,
        username = this.username,
        email = this.email,
        passwordHash = this.passwordHash,
        houseId = this.houseId,
        kudosBalance = this.kudosBalance,
        createdAt = this.createdAt
    )
}
