package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.ActivityLog
import com.houseclash.backend.domain.model.ActivityLogType
import com.houseclash.backend.domain.model.TaskStatus
import com.houseclash.backend.domain.port.ActivityLogRepository
import com.houseclash.backend.domain.port.TaskRepository

class ApplyMarketInflationUsecase(
    private val taskRepository: TaskRepository,
    private val activityLogRepository: ActivityLogRepository,
) {
    fun execute(houseId: Long) {
        val openTasks = taskRepository.findByHouseIdAndStatus(houseId, TaskStatus.OPEN)

        if (openTasks.isNotEmpty()) {
            openTasks.forEach { task ->
                taskRepository.save(task.applyMarketInflation())
            }

            // Use houseId as actorUserId placeholder (system action) — frontend should handle actorUserId=0 as "System"
            activityLogRepository.save(ActivityLog(
                houseId = houseId,
                type = ActivityLogType.MARKET_INFLATION,
                actorUserId = 0L,
                actorUsername = "System"
            ))
        }
    }
}
