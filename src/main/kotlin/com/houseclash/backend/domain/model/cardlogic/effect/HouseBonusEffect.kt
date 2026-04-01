package com.houseclash.backend.domain.model.cardlogic.effect

import com.houseclash.backend.domain.model.cardlogic.CardEffect
import com.houseclash.backend.domain.model.cardlogic.CardEffectContext
import com.houseclash.backend.domain.model.cardlogic.CardEffectResult

class HouseBonusEffect : CardEffect {
    override fun execute(context: CardEffectContext): CardEffectResult {
        val updatedMembers = context.houseMembers.map { it.addKudos(2) }
        return CardEffectResult(
            updatedUsers = updatedMembers,
            description = "All house members received +2 Kudos!"
        )
    }
}
