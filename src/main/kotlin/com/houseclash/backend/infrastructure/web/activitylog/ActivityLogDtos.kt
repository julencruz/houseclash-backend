package com.houseclash.backend.infrastructure.web.activitylog

import com.houseclash.backend.domain.model.ActivityLogType
import com.houseclash.backend.domain.usecase.ActivityLogEntry
import java.time.LocalDateTime

data class ActivityLogResponse(
    val id: Long,
    val houseId: Long,
    val type: ActivityLogType,
    val actorUserId: Long,
    val actorUsername: String,
    val targetUserId: Long?,
    val targetUsername: String?,
    val taskId: Long?,
    val taskTitle: String?,
    val cardType: String?,
    val createdAt: LocalDateTime,
    val isPendingReview: Boolean
)

fun ActivityLogEntry.toResponse() = ActivityLogResponse(
    id = log.id!!,
    houseId = log.houseId,
    type = log.type,
    actorUserId = log.actorUserId,
    actorUsername = log.actorUsername,
    targetUserId = log.targetUserId,
    targetUsername = log.targetUsername,
    taskId = log.taskId,
    taskTitle = log.taskTitle,
    cardType = log.cardType,
    createdAt = log.createdAt,
    isPendingReview = isPendingReview
)
