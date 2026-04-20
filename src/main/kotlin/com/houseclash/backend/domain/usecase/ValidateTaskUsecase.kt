package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.ActivityLog
import com.houseclash.backend.domain.model.ActivityLogType
import com.houseclash.backend.domain.model.Task
import com.houseclash.backend.domain.port.ActivityLogRepository
import com.houseclash.backend.domain.port.TaskRepository
import com.houseclash.backend.domain.port.UserRepository

enum class ValidationDecision {
    APPROVE,
    DISPUTE
}

class ValidateTaskUsecase (
    private val taskRepository: TaskRepository,
    private val userRepository : UserRepository,
    private val activityLogRepository: ActivityLogRepository,
) {
    companion object {
        const val INCENTIVE = 1
    }

    fun execute(taskId: Long, validatorId: Long, decision: ValidationDecision) : Task {
        val task = taskRepository.findById(taskId)
        require(task != null) {"Task doesnt exist"}
        require(task.assignedTo != null) { "Task not assigned" }
        require(task.assignedTo != validatorId) {"Cannot validate your own task"}
        val validator = userRepository.findById(validatorId)
        require(validator != null) {"Validator does not exist"}
        when (decision) {
            ValidationDecision.APPROVE -> {
                val assignedUser = userRepository.findById(task.assignedTo)!!
                if (!task.isCompletedAfterDeadline()) {
                    userRepository.save(assignedUser.addKudos(task.kudosValue))
                }
                val validatedTask = taskRepository.save(task.approveTask())
                userRepository.save(validator.addKudos(INCENTIVE))

                activityLogRepository.save(ActivityLog(
                    houseId = task.houseId,
                    type = ActivityLogType.TASK_APPROVED,
                    actorUserId = validatorId,
                    actorUsername = validator.username,
                    targetUserId = task.assignedTo,
                    targetUsername = assignedUser.username,
                    taskId = task.id,
                    taskTitle = task.title
                ))

                return validatedTask
            }
            ValidationDecision.DISPUTE -> {
                val assignedUser = userRepository.findById(task.assignedTo)!!
                val validatedTask = taskRepository.save(task.disputeTask())

                activityLogRepository.save(ActivityLog(
                    houseId = task.houseId,
                    type = ActivityLogType.TASK_DISPUTED,
                    actorUserId = validatorId,
                    actorUsername = validator.username,
                    targetUserId = task.assignedTo,
                    targetUsername = assignedUser.username,
                    taskId = task.id,
                    taskTitle = task.title
                ))

                return validatedTask
            }
        }
    }
}
