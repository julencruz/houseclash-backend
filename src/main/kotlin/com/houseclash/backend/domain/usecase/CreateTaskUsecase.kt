package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.ActivityLog
import com.houseclash.backend.domain.model.ActivityLogType
import com.houseclash.backend.domain.model.Effort
import com.houseclash.backend.domain.model.Recurrence
import com.houseclash.backend.domain.model.Task
import com.houseclash.backend.domain.port.ActivityLogRepository
import com.houseclash.backend.domain.port.CategoryRepository
import com.houseclash.backend.domain.port.HouseRepository
import com.houseclash.backend.domain.port.TaskRepository
import com.houseclash.backend.domain.port.UserRepository
import java.time.LocalDateTime

class CreateTaskUsecase (
    private val taskRepository : TaskRepository,
    private val houseRepository: HouseRepository,
    private val categoryRepository: CategoryRepository,
    private val userRepository: UserRepository,
    private val activityLogRepository: ActivityLogRepository,
) {
    fun execute(userId: Long, title: String, description: String? = null, effort: Effort, recurrence: Recurrence? = null, deadline: LocalDateTime? = null, houseId: Long, categoryId: Long): Task {
        val user = userRepository.findById(userId)
        require(user != null) { "User not found" }
        require(user.houseId == houseId) { "User does not belong to this house" }

        require(houseRepository.findById(houseId) != null) { "House not found for id: $houseId" }
        val category = categoryRepository.findById(categoryId)
        require(category != null) { "Category doesnt exist" }
        require(category.houseId == houseId) { "Category does not belong to house: $houseId" }

        val task = taskRepository.save(Task.create(title, description, effort, recurrence, deadline, houseId, categoryId))

        activityLogRepository.save(ActivityLog(
            houseId = houseId,
            type = ActivityLogType.TASK_CREATED,
            actorUserId = userId,
            actorUsername = user.username,
            taskId = task.id,
            taskTitle = task.title
        ))

        return task
    }
}
