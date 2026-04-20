package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.ActivityLog
import com.houseclash.backend.domain.model.ActivityLogType
import com.houseclash.backend.domain.model.Task
import com.houseclash.backend.domain.port.ActivityLogRepository
import com.houseclash.backend.domain.port.TaskRepository
import com.houseclash.backend.domain.port.UserRepository


class CompleteTaskUsecase (
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
    private val activityLogRepository: ActivityLogRepository,
) {
    fun execute(taskId: Long, userId: Long) : Task {
        val task = taskRepository.findById(taskId)
        require (task != null) { "Task does not exist" }
        require(task.assignedTo == userId) { "Only the assigned user can complete this task" }

        val user = userRepository.findById(userId)
        require(user != null) { "User does not exist" }

        val completedTask = taskRepository.save(task.completeTask())

        activityLogRepository.save(ActivityLog(
            houseId = task.houseId,
            type = ActivityLogType.TASK_COMPLETED,
            actorUserId = userId,
            actorUsername = user.username,
            taskId = task.id,
            taskTitle = task.title
        ))

        return completedTask
    }
}
