package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.Task
import com.houseclash.backend.domain.port.TaskRepository
import com.houseclash.backend.domain.port.UserRepository

class AssignTaskUsecase (
    private val userRepository : UserRepository,
    private val taskRepository: TaskRepository,
) {
    fun execute(taskId: Long, userId: Long) : Task {
        val user = userRepository.findById(userId)
        require(user != null) {"User doesn´t exist"}
        val task = taskRepository.findById(taskId)
        require(task != null) {"Task doesn´t exist"}
        require(user.houseId == task.houseId) {"User and Task must belong to the same house"}
        val assignedTask = task.assignTaskToUser(userId);
        return taskRepository.save(assignedTask)
    }
}
