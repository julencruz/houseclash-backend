package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.TaskStatus
import com.houseclash.backend.domain.port.ActivityLogRepository
import com.houseclash.backend.domain.port.TaskRepository

class PurgeActivityLogUsecase(
    private val activityLogRepository: ActivityLogRepository,
    private val taskRepository: TaskRepository,
) {
    fun execute(houseId: Long) {
        val pendingTaskIds = taskRepository
            .findByHouseIdAndStatus(houseId, TaskStatus.PENDING_REVIEW)
            .mapNotNull { it.id }

        activityLogRepository.deleteOldLogsExceptPendingReview(houseId, pendingTaskIds)
    }
}
