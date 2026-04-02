package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.MemberStats
import com.houseclash.backend.domain.model.RankingPeriod
import com.houseclash.backend.domain.model.TaskStatus
import com.houseclash.backend.domain.port.TaskRepository
import com.houseclash.backend.domain.port.UserRepository
import java.time.LocalDateTime

class GetHouseRankingUsecase(
    private val userRepository: UserRepository,
    private val taskRepository: TaskRepository,
) {
    fun execute(houseId: Long, period: RankingPeriod): List<MemberStats> {
        val members = userRepository.findByHouseId(houseId)
        require(members.isNotEmpty()) { "No members found in this house" }

        val allTasks = taskRepository.findByHouseId(houseId)
        val cutoff = cutoffDate(period)

        val completedStatuses = setOf(TaskStatus.APPROVED, TaskStatus.AUTO_APPROVED)

        return members
            .map { member ->
                val tasksCompleted = allTasks.count { task ->
                    task.assignedTo == member.id &&
                            task.status in completedStatuses &&
                            (cutoff == null || (task.completedAt != null && task.completedAt.isAfter(cutoff)))
                }
                MemberStats(
                    user = member,
                    kudosBalance = member.kudosBalance,
                    tasksCompleted = tasksCompleted,
                    rank = 0,
                )
            }
            .sortedWith(compareByDescending<MemberStats> { it.kudosBalance }
                .thenByDescending { it.tasksCompleted })
            .mapIndexed { index, stats -> stats.copy(rank = index + 1) }
    }

    private fun cutoffDate(period: RankingPeriod): LocalDateTime? = when (period) {
        RankingPeriod.WEEK -> LocalDateTime.now().minusWeeks(1)
        RankingPeriod.MONTH -> LocalDateTime.now().minusMonths(1)
        RankingPeriod.ALL_TIME -> null
    }
}
