package com.houseclash.backend.domain.model

import java.time.LocalDateTime

enum class ActivityLogType {
    TASK_CREATED,
    TASK_ASSIGNED,
    TASK_UNASSIGNED,
    TASK_COMPLETED,
    TASK_APPROVED,
    TASK_DISPUTED,
    TASK_AUTO_APPROVED,
    CARD_USED,
    MARKET_INFLATION,
    MEMBER_JOINED,
    MEMBER_KICKED,
    CAPTAIN_TRANSFERRED
}

data class ActivityLog(
    val id: Long? = null,
    val houseId: Long,
    val type: ActivityLogType,
    val actorUserId: Long,
    val actorUsername: String,
    val targetUserId: Long? = null,
    val targetUsername: String? = null,
    val taskId: Long? = null,
    val taskTitle: String? = null,
    val cardType: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
