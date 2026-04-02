package com.houseclash.backend.infrastructure.persistence.house

import com.houseclash.backend.domain.model.House
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "houses")
class HouseJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val name: String = "",

    @Column(length = 500)
    val description: String = "",

    @Column(unique = true, nullable = false, length = 6)
    val inviteCode: String = "",

    @Column(name = "created_by", nullable = false)
    val createdBy: Long = 0L,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

fun HouseJpaEntity.toDomain(): House {
    return House(
        id = this.id,
        name = this.name,
        description = this.description,
        inviteCode = this.inviteCode,
        createdBy = this.createdBy,
        createdAt = this.createdAt
    )
}

fun House.toEntity(): HouseJpaEntity {
    return HouseJpaEntity(
        id = this.id,
        name = this.name,
        description = this.description,
        inviteCode = this.inviteCode,
        createdBy = this.createdBy,
        createdAt = this.createdAt
    )
}
