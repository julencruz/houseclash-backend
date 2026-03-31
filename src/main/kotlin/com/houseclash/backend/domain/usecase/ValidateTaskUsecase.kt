package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.Task
import com.houseclash.backend.domain.port.TaskRepository
import com.houseclash.backend.domain.port.UserRepository

enum class ValidationDecision {
    APPROVE,
    DISPUTE
}

class ValidateTaskUsecase (
    private val taskRepository: TaskRepository,
    private val userRepository : UserRepository,
) {
    fun execute(taskId: Long, validatorId: Long, decision: ValidationDecision) : Task {
        val task = taskRepository.findById(taskId)
        require(task != null) {"Task doesn´t exist"}
        require(task.assignedTo != null) { "Task not assigned" }
        require(task.assignedTo != validatorId) {"Cannot validate your own task"}
        when (decision) {
            ValidationDecision.APPROVE -> {
                val assignedUser = userRepository.findById(task.assignedTo!!)!!
                userRepository.save(assignedUser.addKudos(task.kudosValue))
                val validatedTask = task.approveTask()
                return taskRepository.save(validatedTask)
            }
            ValidationDecision.DISPUTE -> {
                val validatedTask = task.disputeTask()
                return taskRepository.save(validatedTask)
            }
        }
    }
}
