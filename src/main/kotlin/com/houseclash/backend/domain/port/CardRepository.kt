package com.houseclash.backend.domain.port

import com.houseclash.backend.domain.model.Card
import com.houseclash.backend.domain.model.CardType

interface CardRepository {

    fun save(card: Card) : Card
    fun findByUserId(userId: Long): List<Card>
    fun findByUserIdAndType(userId: Long, type: CardType): List<Card>
    fun findById(cardId: Long): Card?
    fun delete(cardId: Long)
}
