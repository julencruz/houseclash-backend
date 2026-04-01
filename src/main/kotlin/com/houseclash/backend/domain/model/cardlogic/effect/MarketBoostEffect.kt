package com.houseclash.backend.domain.model.cardlogic.effect

import com.houseclash.backend.domain.model.TaskStatus
import com.houseclash.backend.domain.model.cardlogic.CardEffect
import com.houseclash.backend.domain.model.cardlogic.CardEffectContext
import com.houseclash.backend.domain.model.cardlogic.CardEffectResult

class MarketBoostEffect : CardEffect {
    override fun execute(context: CardEffectContext): CardEffectResult {
        val openTasks = context.houseTasks.filter { it.status == TaskStatus.OPEN }
        val updatedTasks = openTasks.map { it.applyMarketInflation(1) }
        return CardEffectResult(
            updatedTasks = updatedTasks,
            description = "All open tasks received +1 Kudos!"
        )
    }
}
