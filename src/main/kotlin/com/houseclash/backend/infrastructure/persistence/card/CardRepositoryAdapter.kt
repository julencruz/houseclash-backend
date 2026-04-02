package com.houseclash.backend.infrastructure.persistence.card

import com.houseclash.backend.domain.model.Card
import com.houseclash.backend.domain.port.CardRepository
import org.springframework.stereotype.Component

@Component
class CardRepositoryAdapter(
    private val jpaRepository: SpringDataCardRepository
) : CardRepository {

    override fun save(card: Card): Card {
        val entity = card.toEntity()
        return jpaRepository.save(entity).toDomain()
    }

    override fun findById(cardId: Long): Card? {
        return jpaRepository.findById(cardId).map { it.toDomain() }.orElse(null)
    }

    override fun findByUserId(userId: Long): List<Card> {
        return jpaRepository.findByUserId(userId).map { it.toDomain() }
    }

    override fun delete(cardId: Long) {
        jpaRepository.deleteById(cardId)
    }

    override fun deleteByUserId(userId: Long) {
        jpaRepository.deleteByUserId(userId)
    }
}
