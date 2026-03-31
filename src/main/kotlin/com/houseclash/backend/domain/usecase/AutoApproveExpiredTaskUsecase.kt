package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.TaskStatus
import com.houseclash.backend.domain.port.TaskRepository
import com.houseclash.backend.domain.port.UserRepository

class AutoApproveExpiredTasksUsecase(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository
) {
    fun execute(houseId: Long) {
        val pendingTasks = taskRepository.findByHouseIdAndStatus(houseId, TaskStatus.PENDING_REVIEW)

        pendingTasks.filter { it.isValidationExpired() }.forEach { task ->
            val approvedTask = task.autoApproveTask()
            taskRepository.save(approvedTask)

            val user = userRepository.findById(task.assignedTo!!)
            require(user != null) {"User doesn't exist"}
            userRepository.save(user.addKudos(task.kudosValue))

            userRepository.findByHouseId(houseId)
                .filter { it.id != task.assignedTo }
                .forEach { validator ->
                    userRepository.save(validator.penalizeKudos())
                }
        }
    }
}
