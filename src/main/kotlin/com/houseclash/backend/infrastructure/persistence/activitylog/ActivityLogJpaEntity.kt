package com.houseclash.backend.infrastructure.persistence.activitylog

import com.houseclash.backend.domain.model.ActivityLog
import com.houseclash.backend.domain.model.ActivityLogType
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "activity_logs")
class ActivityLogJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "house_id", nullable = false)
    val houseId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: ActivityLogType,

    @Column(name = "actor_user_id", nullable = false)
    val actorUserId: Long,

    @Column(name = "actor_username", nullable = false)
    val actorUsername: String,

    @Column(name = "target_user_id")
    val targetUserId: Long? = null,

    @Column(name = "target_username")
    val targetUsername: String? = null,

    @Column(name = "task_id")
    val taskId: Long? = null,

    @Column(name = "task_title")
    val taskTitle: String? = null,

    @Column(name = "card_type")
    val cardType: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

fun ActivityLogJpaEntity.toDomain() = ActivityLog(
    id = id,
    houseId = houseId,
    type = type,
    actorUserId = actorUserId,
    actorUsername = actorUsername,
    targetUserId = targetUserId,
    targetUsername = targetUsername,
    taskId = taskId,
    taskTitle = taskTitle,
    cardType = cardType,
    createdAt = createdAt
)

fun ActivityLog.toEntity() = ActivityLogJpaEntity(
    id = id,
    houseId = houseId,
    type = type,
    actorUserId = actorUserId,
    actorUsername = actorUsername,
    targetUserId = targetUserId,
    targetUsername = targetUsername,
    taskId = taskId,
    taskTitle = taskTitle,
    cardType = cardType,
    createdAt = createdAt
)
