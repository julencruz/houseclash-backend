package com.houseclash.backend.domain.model.cardlogic.effect

import com.houseclash.backend.domain.model.TaskStatus
import com.houseclash.backend.domain.model.cardlogic.CardEffect
import com.houseclash.backend.domain.model.cardlogic.CardEffectContext
import com.houseclash.backend.domain.model.cardlogic.CardEffectResult

class ForceTaskEffect : CardEffect {
    override fun execute(context: CardEffectContext): CardEffectResult {
        require(context.targetTask != null) { "A task must be selected" }
        require(context.targetUser != null) { "A target user must be selected" }
        require(context.targetTask.status == TaskStatus.OPEN) {
            "Task must be open to force it"
        }
        require(context.targetUser.id != context.executingUser.id) {
            "You cannot force a task to yourself"
        }

        val updatedTask = context.targetTask.copy(
            assignedTo = context.targetUser.id,
            status = TaskStatus.ASSIGNED,
            isForced = true
        )
        return CardEffectResult(
            updatedTasks = listOf(updatedTask),
            description = "Task '${context.targetTask.title}' has been forced to ${context.targetUser.username}!"
        )
    }
}
