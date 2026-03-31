package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.TaskStatus
import com.houseclash.backend.domain.port.TaskRepository

class ApplyMarketInflationUsecase(
    private val taskRepository: TaskRepository
) {
    fun execute(houseId: Long) {
        val openTasks = taskRepository.findByHouseIdAndStatus(houseId, TaskStatus.OPEN)

        openTasks.forEach { task ->
            val updatedTask = task.applyMarketInflation()
            taskRepository.save(updatedTask)
        }
    }
}
