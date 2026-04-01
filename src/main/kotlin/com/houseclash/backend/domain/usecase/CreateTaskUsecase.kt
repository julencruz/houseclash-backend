package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.Effort
import com.houseclash.backend.domain.model.Recurrence
import com.houseclash.backend.domain.model.Task
import com.houseclash.backend.domain.port.CategoryRepository
import com.houseclash.backend.domain.port.HouseRepository
import com.houseclash.backend.domain.port.TaskRepository

class CreateTaskUsecase (
    private val taskRepository : TaskRepository,
    private val houseRepository: HouseRepository,
    private val categoryRepository: CategoryRepository
) {
    fun execute(title: String, description: String? = null, effort: Effort, recurrence: Recurrence? = null, houseId: Long, categoryId: Long): Task {
        require(houseRepository.findById(houseId) != null) { "House not found for id: $houseId" }
        val category = categoryRepository.findById(categoryId)
        require(category != null) { "Category doesnt exist" }
        require(category.houseId == houseId) { "Category does not belong to house: $houseId" }

        val task = Task.create(title, description, effort, recurrence, houseId, categoryId)
        return taskRepository.save(task)
    }
}
