package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.Effort
import com.houseclash.backend.domain.model.Task
import com.houseclash.backend.domain.port.HouseRepository
import com.houseclash.backend.domain.port.TaskRepository

class CreateTaskUsecase (
    private val taskRepository : TaskRepository,
    private val houseRepository: HouseRepository
) {
    fun execute(title: String, description: String? = null, effort: Effort, houseId: Long): Task {
        require(houseRepository.findById(houseId) != null) { "House not found for id: $houseId" }
        val task = Task.create(title, description, effort, houseId)
        return taskRepository.save(task)
    }
}
