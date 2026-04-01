package com.houseclash.backend.domain.model.cardlogic.effect

import com.houseclash.backend.domain.model.TaskStatus
import com.houseclash.backend.domain.model.cardlogic.CardEffect
import com.houseclash.backend.domain.model.cardlogic.CardEffectContext
import com.houseclash.backend.domain.model.cardlogic.CardEffectResult

class CleanSlateEffect : CardEffect {
    override fun execute(context: CardEffectContext): CardEffectResult {
        val forcedTask = context.houseTasks.firstOrNull {
            it.assignedTo == context.executingUser.id && it.isForced
        }

        require(forcedTask != null) { "You have no forced tasks to remove" }

        val updatedTask = forcedTask.copy(
            assignedTo = null,
            status = TaskStatus.OPEN,
            isForced = false
        )
        return CardEffectResult(
            updatedTasks = listOf(updatedTask),
            description = "Your forced task '${forcedTask.title}' has been returned to the market!"
        )
    }
}
