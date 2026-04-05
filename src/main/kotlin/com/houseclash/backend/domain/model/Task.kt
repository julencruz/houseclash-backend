package com.houseclash.backend.domain.model

import java.time.LocalDateTime
import java.time.Period

enum class Recurrence(val period: Period) {
    DAILY(Period.ofDays(1)),
    WEEKLY(Period.ofWeeks(1)),
    BIWEEKLY(Period.ofWeeks(2)),
    MONTHLY(Period.ofMonths(1))
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

data class Task(
    val id: Long? = null,
    val title: String,
    val description: String? = null,
    val effort: Effort,
    val status: TaskStatus = TaskStatus.OPEN,
    val kudosValue: Int = effort.baseKudos,
    val assignedTo: Long? = null,
    val houseId: Long,
    val categoryId: Long,
    val isForced: Boolean = false,
    val recurrence: Recurrence? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val completedAt: LocalDateTime? = null,
    val version: Long = 0
) {
    companion object {
        fun create(title: String, description: String?, effort: Effort, recurrence: Recurrence? = null, houseId: Long, categoryId: Long): Task {
            require(title.isNotBlank()) { "Title cannot be blank" }
            return Task(
                title = title,
                description = description,
                effort = effort,
                recurrence = recurrence,
                houseId = houseId,
                categoryId = categoryId
            )
        }
    }

    fun assignTaskToUser(userId: Long): Task {
        require(status == TaskStatus.OPEN) { "Task is not open for assignment" }
        return this.copy(
            assignedTo = userId,
            status = TaskStatus.ASSIGNED
        )
    }

    fun unassignTask(userId: Long): Task {
        require(status == TaskStatus.ASSIGNED) { "Only assigned tasks can be unassigned" }
        require(!isForced) { "Cannot unassign a task that was forced upon you" }
        require(userId == assignedTo) { "Only the user assigned to the task can unassign it" }
        return this.copy(
            status = TaskStatus.OPEN,
            assignedTo = null
        )
    }

    fun forceAssignTo(userId: Long): Task {
        require(status == TaskStatus.OPEN) { "Only open tasks can be force-assigned" }
        return this.copy(
            status = TaskStatus.ASSIGNED,
            assignedTo = userId,
            isForced = true
        )
    }

    fun completeTask() : Task {
        require(status == TaskStatus.ASSIGNED) { "Task must be assigned to be completed" }
        return this.copy(
            status = TaskStatus.PENDING_REVIEW,
            completedAt = LocalDateTime.now()
        )
    }

    fun approveTask() : Task {
        require(status == TaskStatus.PENDING_REVIEW) { "Task is not pending review" }
        return this.copy(
            status = TaskStatus.APPROVED
        )
    }

    fun disputeTask() : Task {
        require(status == TaskStatus.PENDING_REVIEW) { "Task is not pending review" }
        return this.copy(
            status = TaskStatus.DISPUTED
        )
    }

    fun validationDeadline(): LocalDateTime? {
        return completedAt?.plusHours(24)
    }

    fun isValidationExpired(): Boolean {
        return validationDeadline()?.isBefore(LocalDateTime.now()) ?: false
    }

    fun autoApproveTask() : Task {
        require(status == TaskStatus.PENDING_REVIEW) { "Task is not pending review" }
        require(isValidationExpired()) { "Validation deadline has not expired yet" }
        return this.copy(
            status = TaskStatus.AUTO_APPROVED
        )
    }

    fun currentCycleStart(now: LocalDateTime = LocalDateTime.now()): LocalDateTime? {
        if (recurrence == null) return null
        var cycleStart = createdAt
        while (cycleStart.plus(recurrence.period).isBefore(now)) {
            cycleStart = cycleStart.plus(recurrence.period)
        }
        return cycleStart
    }

    fun isDueForReset(now: LocalDateTime = LocalDateTime.now()): Boolean {
        if (recurrence == null) return false
        val isClosedStatus = status in listOf(
            TaskStatus.APPROVED,
            TaskStatus.AUTO_APPROVED,
            TaskStatus.DISPUTED
        )
        val completedInPreviousCycle = completedAt?.isBefore(currentCycleStart(now)!!) ?: false
        return isClosedStatus && completedInPreviousCycle
    }

    fun resetForNextCycle(): Task {
        require(recurrence != null) { "Task is not recurrent" }
        return this.copy(
            status = TaskStatus.OPEN,
            assignedTo = null,
            completedAt = null,
            kudosValue = effort.baseKudos
        )
    }

    fun applyMarketInflation(increment: Int? = null): Task {
        val value = increment
            ?: when (effort) {
                Effort.LOW -> 1
                Effort.MEDIUM -> 2
                Effort.HIGH -> 3
            }

        return this.copy(kudosValue = kudosValue + value)
    }

    fun update(
        newTitle: String? = null,
        newDescription: String? = null,
        newEffort: Effort? = null,
        newRecurrence: Recurrence? = null,
        newCategoryId: Long? = null
    ): Task {
        val updatedEffort = newEffort ?: this.effort
        val updatedKudosValue = if (newEffort != null && newEffort != this.effort) {
            newEffort.baseKudos
        } else {
            this.kudosValue
        }

        return this.copy(
            title = newTitle ?: this.title,
            description = newDescription ?: this.description,
            effort = updatedEffort,
            kudosValue = updatedKudosValue,
            recurrence = newRecurrence ?: this.recurrence,
            categoryId = newCategoryId ?: this.categoryId
        )
    }
}
