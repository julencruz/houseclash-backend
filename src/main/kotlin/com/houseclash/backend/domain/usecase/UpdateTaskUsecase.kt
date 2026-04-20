package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.Effort
import com.houseclash.backend.domain.model.Recurrence
import com.houseclash.backend.domain.model.Task
import com.houseclash.backend.domain.port.CategoryRepository
import com.houseclash.backend.domain.port.TaskRepository
import com.houseclash.backend.domain.port.UserRepository
import java.time.LocalDateTime

class UpdateTaskUsecase(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
    private val categoryRepository: CategoryRepository
) {
    fun execute(
        userId: Long,
        taskId: Long,
        title: String? = null,
        description: String? = null,
        effort: Effort? = null,
        recurrence: String? = null,
        deadline: LocalDateTime? = null,
        clearDeadline: Boolean = false,
        categoryId: Long? = null
    ): Task {
        val user = userRepository.findById(userId)
        requireNotNull(user) { "User not found" }

        val task = taskRepository.findById(taskId)
        requireNotNull(task) { "Task not found" }

        require(task.houseId == user.houseId) { "Cannot update tasks outside your house" }

        if (categoryId != null && categoryId != task.categoryId) {
            val category = categoryRepository.findById(categoryId)
            requireNotNull(category) { "Category not found" }
            require(category.houseId == task.houseId) { "Target category does not belong to this house" }
        }


        val newRecurrence = if (recurrence != null) {
            try {
                Recurrence.valueOf(recurrence)
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Invalid recurrence value")
            }
        } else {
            task.recurrence
        }

        val updatedTask = task.update(
            newTitle = title ?: task.title,
            newDescription = description ?: task.description,
            newEffort = effort,
            newRecurrence = newRecurrence,
            newDeadline = deadline,
            clearDeadline = clearDeadline,
            newCategoryId = categoryId ?: task.categoryId
        )

        return taskRepository.save(updatedTask)
    }
}
