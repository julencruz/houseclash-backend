package com.houseclash.backend.domain.model.cardlogic

interface CardEffect {
    fun execute(context: CardEffectContext): CardEffectResult
}
