package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.Task
import com.houseclash.backend.domain.port.TaskRepository


class CompleteTaskUsecase (
    private val taskRepository: TaskRepository,
) {
    fun execute(taskId: Long) : Task {
        val task = taskRepository.findById(taskId)
        require (task != null) { "Task does not exist" }
        return taskRepository.save(task.completeTask())
    }
}
