package com.houseclash.backend.domain.model.cardlogic.effect

import com.houseclash.backend.domain.model.TaskStatus
import com.houseclash.backend.domain.model.cardlogic.CardEffect
import com.houseclash.backend.domain.model.cardlogic.CardEffectContext
import com.houseclash.backend.domain.model.cardlogic.CardEffectResult

class SkipTaskEffect : CardEffect {
    override fun execute(context: CardEffectContext): CardEffectResult {
        require(context.targetTask != null) { "A task must be selected" }
        require(context.targetTask.assignedTo == context.executingUser.id) {
            "You can only skip your own tasks"
        }
        require(context.targetTask.status == TaskStatus.ASSIGNED) {
            "Task must be assigned to skip it"
        }

        val updatedTask = context.targetTask.copy(
            assignedTo = null,
            status = TaskStatus.OPEN,
            isForced = false
        )
        return CardEffectResult(
            updatedTasks = listOf(updatedTask),
            description = "Task '${context.targetTask.title}' returned to market without penalty!"
        )
    }
}
