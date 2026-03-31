package com.houseclash.backend.domain.port

import com.houseclash.backend.domain.model.Task
import com.houseclash.backend.domain.model.TaskStatus

interface TaskRepository {

    fun save(task: Task) : Task
    fun findById(id: Long) : Task?
    fun findByHouseId(houseId: Long) : List<Task>
    fun findByHouseIdAndStatus(houseId: Long, status: TaskStatus) : List<Task>
    fun findByAssignedTo(userId: Long) : List<Task>
    fun findRecurringTasksDue(): List<Task>
    fun delete(id: Long)

}
