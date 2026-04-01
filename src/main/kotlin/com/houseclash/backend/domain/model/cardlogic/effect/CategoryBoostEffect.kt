package com.houseclash.backend.domain.model.cardlogic.effect

import com.houseclash.backend.domain.model.TaskStatus
import com.houseclash.backend.domain.model.cardlogic.CardEffect
import com.houseclash.backend.domain.model.cardlogic.CardEffectContext
import com.houseclash.backend.domain.model.cardlogic.CardEffectResult

class CategoryBoostEffect : CardEffect {
    override fun execute(context: CardEffectContext): CardEffectResult {
        require(context.targetCategoryId != null) { "A category must be selected" }

        val eligibleTasks = context.houseTasks.filter {
            it.categoryId == context.targetCategoryId &&
                    it.status != TaskStatus.APPROVED &&
                    it.status != TaskStatus.AUTO_APPROVED
        }

        val updatedTasks = eligibleTasks.map { it.applyMarketInflation(1) }
        return CardEffectResult(
            updatedTasks = updatedTasks,
            description = "All tasks in the selected category received +1 Kudos!"
        )
    }
}
