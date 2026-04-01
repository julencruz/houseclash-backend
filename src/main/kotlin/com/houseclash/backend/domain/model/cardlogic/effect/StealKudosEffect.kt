package com.houseclash.backend.domain.model.cardlogic.effect

import com.houseclash.backend.domain.model.cardlogic.CardEffect
import com.houseclash.backend.domain.model.cardlogic.CardEffectContext
import com.houseclash.backend.domain.model.cardlogic.CardEffectResult

class StealKudosEffect : CardEffect {
    companion object {
        const val STEAL_AMOUNT = 5
    }

    override fun execute(context: CardEffectContext): CardEffectResult {
        require(context.targetUser != null) { "A target user must be selected" }
        require(context.targetUser.id != context.executingUser.id) {
            "You cannot steal from yourself"
        }

        val updatedTarget = context.targetUser.penalizeKudos(STEAL_AMOUNT)
        val updatedExecutor = context.executingUser.addKudos(STEAL_AMOUNT)

        return CardEffectResult(
            updatedUsers = listOf(updatedTarget, updatedExecutor),
            description = "You stole $STEAL_AMOUNT Kudos from ${context.targetUser.username}!"
        )
    }
}
