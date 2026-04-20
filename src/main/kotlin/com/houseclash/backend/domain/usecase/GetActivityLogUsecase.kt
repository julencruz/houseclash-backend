package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.ActivityLog
import com.houseclash.backend.domain.model.ActivityLogType
import com.houseclash.backend.domain.model.TaskStatus
import com.houseclash.backend.domain.port.ActivityLogRepository
import com.houseclash.backend.domain.port.TaskRepository

class GetActivityLogUsecase(
    private val activityLogRepository: ActivityLogRepository,
    private val taskRepository: TaskRepository,
) {
    fun execute(houseId: Long): List<ActivityLogEntry> {
        val logs = activityLogRepository.findByHouseIdOrderByCreatedAtAsc(houseId)
        val pendingTaskIds = taskRepository
            .findByHouseIdAndStatus(houseId, TaskStatus.PENDING_REVIEW)
            .map { it.id!! }
            .toSet()

        return logs.map { log ->
            ActivityLogEntry(
                log = log,
                isPendingReview = log.taskId != null && log.taskId in pendingTaskIds
                        && log.type == ActivityLogType.TASK_COMPLETED
            )
        }
    }
}

data class ActivityLogEntry(
    val log: ActivityLog,
    val isPendingReview: Boolean
)
