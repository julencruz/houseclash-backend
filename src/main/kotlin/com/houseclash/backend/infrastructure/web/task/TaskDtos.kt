package com.houseclash.backend.infrastructure.web.task

import com.houseclash.backend.domain.model.Category
import com.houseclash.backend.domain.model.Effort
import com.houseclash.backend.domain.model.Recurrence
import com.houseclash.backend.domain.model.Task
import com.houseclash.backend.domain.model.TaskStatus
import java.time.LocalDateTime

 

data class CreateTaskRequest(
    val title: String,
    val description: String? = null,
    val effort: Effort,
    val recurrence: String? = null,
    val deadline: LocalDateTime? = null,
    val houseId: Long,
    val categoryId: Long? = null
)

data class UpdateTaskRequest(
    val title: String? = null,
    val description: String? = null,
    val clearDescription: Boolean = false,
    val effort: Effort? = null,
    val recurrence: String? = null,
    val clearRecurrence: Boolean = false,
    val deadline: LocalDateTime? = null,
    val clearDeadline: Boolean = false,
    val categoryId: Long? = null
)

data class ValidateTaskRequest(
    val decision: String  // "APPROVE" | "DISPUTE"
)

 

data class CategorySummary(
    val id: Long,
    val name: String,
    val isDefault: Boolean
)

data class TaskResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val effort: Effort,
    val status: TaskStatus,
    val kudosValue: Int,
    val assignedTo: Long?,
    val houseId: Long,
    val category: CategorySummary,
    val isForced: Boolean,
    val recurrence: Recurrence?,
    val deadline: LocalDateTime?,
    val createdAt: LocalDateTime,
    val completedAt: LocalDateTime?
)

 

fun Task.toResponse(category: Category) = TaskResponse(
    id = this.id!!,
    title = this.title,
    description = this.description,
    effort = this.effort,
    status = this.status,
    kudosValue = this.kudosValue,
    assignedTo = this.assignedTo,
    houseId = this.houseId,
    category = CategorySummary(
        id = category.id!!,
        name = category.name,
        isDefault = category.isDefault
    ),
    isForced = this.isForced,
    recurrence = this.recurrence,
    deadline = this.deadline,
    createdAt = this.createdAt,
    completedAt = this.completedAt
)
