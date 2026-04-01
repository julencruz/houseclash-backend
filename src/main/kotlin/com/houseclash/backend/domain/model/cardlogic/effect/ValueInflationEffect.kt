package com.houseclash.backend.domain.model.cardlogic.effect

import com.houseclash.backend.domain.model.TaskStatus
import com.houseclash.backend.domain.model.cardlogic.CardEffect
import com.houseclash.backend.domain.model.cardlogic.CardEffectContext
import com.houseclash.backend.domain.model.cardlogic.CardEffectResult

class ValueInflationEffect : CardEffect {
    override fun execute(context: CardEffectContext): CardEffectResult {
        require(context.targetTask != null) { "A task must be selected" }
        require(context.targetTask.status == TaskStatus.OPEN) {
            "Task must be open to inflate its value"
        }

        val updatedTask = context.targetTask.copy(
            kudosValue = context.targetTask.kudosValue * 2
        )
        return CardEffectResult(
            updatedTasks = listOf(updatedTask),
            description = "Task '${context.targetTask.title}' value doubled to ${updatedTask.kudosValue} Kudos!"
        )
    }
}
