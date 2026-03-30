package com.houseclash.backend.domain.model

import java.time.LocalDateTime

data class Task(
    val id: Long? = null,
    val title: String,
    val description: String? = null,
    val effort: Effort,
    val status: TaskStatus = TaskStatus.OPEN,
    val kudosValue: Int = effort.baseKudos,
    val assignedTo: Long? = null,
    val houseId: Long,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val completedAt: LocalDateTime? = null
) {
    companion object {
        fun create(title: String, description: String?, effort: Effort, houseId: Long): Task {
            require(title.isNotBlank()) { "Title cannot be blank" }
            return Task(
                title = title,
                description = description,
                effort = effort,
                houseId = houseId
            )
        }
    }
}

enum class Effort(val baseKudos: Int) {
    LOW(2),      // Simple tasks, such as taking out the trash or doing the dishes
    MEDIUM(4),   // Moderate tasks, such as cleaning the bathroom or doing the groceries
    HIGH(8)      // Complex tasks, such as painting a room or repairing an appliance
}

enum class TaskStatus {
    OPEN,            // Available for assignment
    ASSIGNED,        // The user has accepted the task
    PENDING_REVIEW,  // Pending validation
    APPROVED,        // Validated by peers
    AUTO_APPROVED,   // Automatically validated after 24h
    DISPUTED         // Someone has denied the validation
}
