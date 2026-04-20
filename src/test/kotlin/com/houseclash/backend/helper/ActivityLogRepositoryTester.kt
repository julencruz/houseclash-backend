package com.houseclash.backend.helper

import com.houseclash.backend.domain.model.ActivityLog
import com.houseclash.backend.domain.port.ActivityLogRepository

class ActivityLogRepositoryTester : ActivityLogRepository {
    val logs = mutableListOf<ActivityLog>()
    private var idCounter = 1L

    override fun save(log: ActivityLog): ActivityLog {
        val saved = if (log.id == null) log.copy(id = idCounter++) else log
        logs.removeIf { it.id == saved.id }
        logs.add(saved)
        return saved
    }

    override fun findByHouseIdOrderByCreatedAtAsc(houseId: Long): List<ActivityLog> {
        return logs.filter { it.houseId == houseId }.sortedBy { it.createdAt }
    }

    override fun deleteOldLogsExceptPendingReview(houseId: Long, pendingTaskIds: List<Long>) {
        if (pendingTaskIds.isEmpty()) {
            logs.removeIf { it.houseId == houseId }
        } else {
            logs.removeIf { it.houseId == houseId && (it.taskId == null || it.taskId !in pendingTaskIds) }
        }
    }
}
