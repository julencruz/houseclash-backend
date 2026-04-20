package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.ActivityLog
import com.houseclash.backend.domain.model.ActivityLogType
import com.houseclash.backend.domain.model.Task
import com.houseclash.backend.domain.port.ActivityLogRepository
import com.houseclash.backend.domain.port.TaskRepository
import com.houseclash.backend.domain.port.UserRepository

class AssignTaskUsecase (
    private val userRepository : UserRepository,
    private val taskRepository: TaskRepository,
    private val activityLogRepository: ActivityLogRepository,
) {
    fun execute(taskId: Long, userId: Long) : Task {
        val user = userRepository.findById(userId)
        require(user != null) {"User doesnt exist"}
        val task = taskRepository.findById(taskId)
        require(task != null) {"Task doesnt exist"}
        require(user.houseId == task.houseId) {"User and Task must belong to the same house"}
        val assignedTask = taskRepository.save(task.assignTaskToUser(userId))

        activityLogRepository.save(ActivityLog(
            houseId = task.houseId,
            type = ActivityLogType.TASK_ASSIGNED,
            actorUserId = userId,
            actorUsername = user.username,
            taskId = task.id,
            taskTitle = task.title
        ))

        return assignedTask
    }
}
