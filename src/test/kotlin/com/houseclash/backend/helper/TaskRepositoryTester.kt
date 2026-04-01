package com.houseclash.backend.helper

import com.houseclash.backend.domain.model.Task
import com.houseclash.backend.domain.model.TaskStatus
import com.houseclash.backend.domain.port.TaskRepository

class TaskRepositoryTester : TaskRepository {
    private val tasks = mutableListOf<Task>()
    private var idCounter = 1L

    override fun save(task: Task): Task {
        val savedTask = if (task.id == null) {
            task.copy(id = idCounter++)
        } else {
            task
        }
        tasks.removeIf { it.id == savedTask.id }
        tasks.add(savedTask)
        return savedTask
    }

    override fun findById(id: Long): Task? {
        return tasks.find { it.id == id }
    }

    override fun findByHouseId(houseId: Long): List<Task> {
        return tasks.filter { it.houseId == houseId }
    }

    override fun findByHouseIdAndStatus(houseId: Long, status: TaskStatus): List<Task> {
        return tasks.filter { it.status == status && it.houseId == houseId }
    }

    override fun findByAssignedTo(userId: Long): List<Task> {
        return tasks.filter { it.assignedTo == userId }
    }

    override fun findRecurringTasksDue(): List<Task> {
        return tasks.filter {
            it.recurrence != null && it.isDueForReset()
        }
    }

    override fun findByCategoryId(categoryId: Long): List<Task> {
        return tasks.filter { it.categoryId == categoryId }
    }

    override fun deleteByHouseId(houseId: Long) {
        val keysToRemove = tasks.filter { it.houseId == houseId }
        keysToRemove.forEach { tasks.remove(it) }
    }

    override fun delete(id: Long) {
        tasks.removeIf { it.id == id }
    }
}
