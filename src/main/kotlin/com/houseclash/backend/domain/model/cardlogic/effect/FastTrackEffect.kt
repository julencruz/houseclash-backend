package com.houseclash.backend.domain.model.cardlogic.effect

import com.houseclash.backend.domain.model.TaskStatus
import com.houseclash.backend.domain.model.cardlogic.CardEffect
import com.houseclash.backend.domain.model.cardlogic.CardEffectContext
import com.houseclash.backend.domain.model.cardlogic.CardEffectResult

class FastTrackEffect : CardEffect {
    override fun execute(context: CardEffectContext): CardEffectResult {
        val pendingTasks = context.houseTasks
            .filter { it.status == TaskStatus.PENDING_REVIEW }

        require(pendingTasks.isNotEmpty()) { "No tasks pending review to fast-track" }

        val updatedTasks = pendingTasks.map { task ->
            task.copy(status = TaskStatus.APPROVED)
        }

        val updatedUsers = pendingTasks.mapNotNull { task ->
            if (task.isCompletedAfterDeadline()) return@mapNotNull null
            val assignee = context.houseMembers.find { it.id == task.assignedTo }
                ?: return@mapNotNull null
            assignee.addKudos(task.kudosValue)
        }

        return CardEffectResult(
            updatedTasks = updatedTasks,
            updatedUsers = updatedUsers,
            description = "Fast-tracked ${updatedTasks.size} pending task(s)! Kudos awarded to all eligible assignees."
        )
    }
}
