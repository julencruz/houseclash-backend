package com.houseclash.backend.infrastructure.persistence.task

import com.houseclash.backend.domain.model.Effort
import com.houseclash.backend.domain.model.Recurrence
import com.houseclash.backend.domain.model.Task
import com.houseclash.backend.domain.model.TaskStatus
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "tasks")
class TaskJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Version
    val version: Long = 0,

    @Column(nullable = false)
    val title: String,

    @Column(length = 500)
    val description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val effort: Effort,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: TaskStatus,

    @Column(nullable = false)
    val kudosValue: Int,

    @Column(name = "assigned_to")
    val assignedTo: Long? = null,

    @Column(name = "house_id", nullable = false)
    val houseId: Long,

    @Column(name = "category_id", nullable = false)
    val categoryId: Long,

    @Column(nullable = false)
    val isForced: Boolean = false,

    @Enumerated(EnumType.STRING)
    val recurrence: Recurrence? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime,

    val completedAt: LocalDateTime? = null
)

fun TaskJpaEntity.toDomain(): Task {
    return Task(
        id = this.id,
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
        completedAt = this.completedAt,
        version = this.version
    )
}

fun Task.toEntity(): TaskJpaEntity {
    return TaskJpaEntity(
        id = this.id,
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
        completedAt = this.completedAt,
        version = this.version
    )
}
