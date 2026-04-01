package com.houseclash.backend.helper

import com.houseclash.backend.domain.model.Card
import com.houseclash.backend.domain.model.CardType
import com.houseclash.backend.domain.port.CardRepository

class CardRepositoryTester : CardRepository {
    private val cards = mutableListOf<Card>()
    private var idCounter = 1L

    override fun save(card: Card): Card {
        val savedCard = if (card.id == null) {
            card.copy(id = idCounter++)
        } else {
            card
        }
        cards.removeIf { it.id == savedCard.id }
        cards.add(savedCard)
        return savedCard
    }

    override fun findByUserId(userId: Long): List<Card> {
        return cards.filter { it.userId == userId }
    }

    override fun findByUserIdAndType(
        userId: Long,
        type: CardType
    ): List<Card> {
        return cards.filter { it.userId == userId && it.type == type }
    }

    override fun deleteByUserId(userId: Long) {
        val keysToRemove = cards.filter { it.userId == userId }
        keysToRemove.forEach { cards.remove(it) }
    }

    override fun findById(cardId: Long): Card? {
        return cards.find { it.id == cardId }
    }

    override fun delete(cardId: Long) {
        cards.removeIf { it.id == cardId }
    }
}
