package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.ActivityLog
import com.houseclash.backend.domain.model.ActivityLogType
import com.houseclash.backend.domain.model.Task
import com.houseclash.backend.domain.model.TaskStatus
import com.houseclash.backend.domain.port.ActivityLogRepository
import com.houseclash.backend.domain.port.TaskRepository
import com.houseclash.backend.domain.port.UserRepository

class UnassignTaskUsecase (
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
    private val activityLogRepository: ActivityLogRepository,
) {
    companion object {
        const val PENALTY = 3
    }

    fun execute(userId: Long, taskId: Long): Task {
        val task = taskRepository.findById(taskId)
        require(task != null) {"Task does not exist"}
        require(task.status == TaskStatus.ASSIGNED) {"Task is not currently assigned"}
        require(!task.isForced) {"Cannot unassign forced task"}
        require(task.assignedTo == userId) { "Cannot unassign task assigned to another user" }

        val user = userRepository.findById(userId)
        require(user != null) {"User does not exist"}

        userRepository.save(user.penalizeKudos(PENALTY))
        val unassignedTask = taskRepository.save(task.unassignTask(userId))

        activityLogRepository.save(ActivityLog(
            houseId = task.houseId,
            type = ActivityLogType.TASK_UNASSIGNED,
            actorUserId = userId,
            actorUsername = user.username,
            taskId = task.id,
            taskTitle = task.title
        ))

        return unassignedTask
    }
}
