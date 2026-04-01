package com.houseclash.backend.domain.model.cardlogic.effect

import com.houseclash.backend.domain.model.TaskStatus
import com.houseclash.backend.domain.model.cardlogic.CardEffect
import com.houseclash.backend.domain.model.cardlogic.CardEffectContext
import com.houseclash.backend.domain.model.cardlogic.CardEffectResult
import java.time.LocalDateTime

class GracePeriodEffect : CardEffect {
    override fun execute(context: CardEffectContext): CardEffectResult {
        val pendingTasks = context.houseTasks
            .filter { it.status == TaskStatus.PENDING_REVIEW }

        require(pendingTasks.isNotEmpty()) { "No tasks pending review to extend" }

        val now = LocalDateTime.now()
        val updatedTasks = pendingTasks.map { task ->
            task.copy(completedAt = now)
        }

        return CardEffectResult(
            updatedTasks = updatedTasks,
            updatedUsers = emptyList(),
            description = "Validation deadlines reset for ${updatedTasks.size} pending tasks. House saved from penalties!"
        )
    }
}
