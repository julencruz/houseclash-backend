package com.houseclash.backend.infrastructure.web.task

import com.houseclash.backend.domain.model.Effort
import com.houseclash.backend.domain.model.Recurrence
import com.houseclash.backend.domain.model.Task
import com.houseclash.backend.domain.model.TaskStatus
import java.time.LocalDateTime

// --- REQUESTS ---

data class CreateTaskRequest(
    val title: String,
    val description: String? = null,
    val effort: Effort,
    val recurrence: String? = null,
    val houseId: Long,
    val categoryId: Long
)

data class UpdateTaskRequest(
    val title: String? = null,
    val description: String? = null,
    val effort: Effort? = null,
    val recurrence: String? = null,
    val categoryId: Long? = null
)

data class ValidateTaskRequest(
    val decision: String  // "APPROVE" | "DISPUTE"
)

// --- RESPONSES ---

data class TaskResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val effort: Effort,
    val status: TaskStatus,
    val kudosValue: Int,
    val assignedTo: Long?,
    val houseId: Long,
    val categoryId: Long,
    val isForced: Boolean,
    val recurrence: Recurrence?,
    val createdAt: LocalDateTime,
    val completedAt: LocalDateTime?
)

// --- MAPPERS ---

fun Task.toResponse() = TaskResponse(
    id = this.id!!,
    title = this.title,
    description = this.description,
    effort = this.effort,
    status = this.status,
    kudosValue = this.kudosValue,
    assignedTo = this.assignedTo,
    houseId = this.houseId,
    categoryId = this.categoryId,
    isForced = this.isForced,
    recurrence = this.recurrence,
    createdAt = this.createdAt,
    completedAt = this.completedAt
)
