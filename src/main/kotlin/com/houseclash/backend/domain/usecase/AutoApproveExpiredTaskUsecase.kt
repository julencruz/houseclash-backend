package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.ActivityLog
import com.houseclash.backend.domain.model.ActivityLogType
import com.houseclash.backend.domain.model.TaskStatus
import com.houseclash.backend.domain.port.ActivityLogRepository
import com.houseclash.backend.domain.port.TaskRepository
import com.houseclash.backend.domain.port.UserRepository

class AutoApproveExpiredTasksUsecase(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
    private val activityLogRepository: ActivityLogRepository,
) {
    fun execute(houseId: Long) {
        val pendingTasks = taskRepository.findByHouseIdAndStatus(houseId, TaskStatus.PENDING_REVIEW)

        pendingTasks.filter { it.isValidationExpired() }.forEach { task ->
            val approvedTask = task.autoApproveTask()
            taskRepository.save(approvedTask)

            val user = userRepository.findById(task.assignedTo!!)
            require(user != null) {"User doesn't exist"}
            if (!task.isCompletedAfterDeadline()) {
                userRepository.save(user.addKudos(task.kudosValue))
            }

            activityLogRepository.save(ActivityLog(
                houseId = houseId,
                type = ActivityLogType.TASK_AUTO_APPROVED,
                actorUserId = task.assignedTo,
                actorUsername = user.username,
                taskId = task.id,
                taskTitle = task.title
            ))
        }
    }
}
