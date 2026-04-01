package com.houseclash.backend.domain.model.cardlogic.effect

import com.houseclash.backend.domain.model.TaskStatus
import com.houseclash.backend.domain.model.cardlogic.CardEffect
import com.houseclash.backend.domain.model.cardlogic.CardEffectContext
import com.houseclash.backend.domain.model.cardlogic.CardEffectResult

class UnderdogBoostEffect : CardEffect {
    override fun execute(context: CardEffectContext): CardEffectResult {
        val lowestTask = context.houseTasks
            .filter { it.status == TaskStatus.OPEN }
            .minByOrNull { it.kudosValue }

        require(lowestTask != null) { "No open tasks in this house" }

        val updatedTask = lowestTask.applyMarketInflation(3)
        return CardEffectResult(
            updatedTasks = listOf(updatedTask),
            description = "Task '${lowestTask.title}' received +3 Kudos!"
        )
    }
}
