package com.houseclash.backend.infrastructure.persistence.card

import com.houseclash.backend.domain.model.Card
import com.houseclash.backend.domain.model.CardType
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "cards")
class CardJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "user_id", nullable = false)
    val userId: Long = 0L,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: CardType,

    @Column(nullable = false, updatable = false)
    val acquiredAt: LocalDateTime = LocalDateTime.now()
)

fun CardJpaEntity.toDomain(): Card {
    return Card(
        id = this.id,
        userId = this.userId,
        type = this.type,
        acquiredAt = this.acquiredAt
    )
}

fun Card.toEntity(): CardJpaEntity {
    return CardJpaEntity(
        id = this.id,
        userId = this.userId,
        type = this.type,
        acquiredAt = this.acquiredAt
    )
}
