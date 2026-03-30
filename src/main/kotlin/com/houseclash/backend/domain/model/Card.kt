package com.houseclash.backend.domain.model

import com.houseclash.backend.domain.model.cardlogic.CardEffect
import com.houseclash.backend.domain.model.cardlogic.effect.ExampleEffect
import java.time.LocalDateTime

data class Card (
    val id: Long? = null,
    val userId: Long,
    val type: CardType,
    val acquiredAt: LocalDateTime = LocalDateTime.now()
)

enum class CardType(val effect: CardEffect, val probability: Int) {
    EXAMPLE_CARD(ExampleEffect(), 100)
}
