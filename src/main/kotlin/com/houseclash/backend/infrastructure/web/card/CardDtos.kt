package com.houseclash.backend.infrastructure.web.card

import com.houseclash.backend.domain.model.Card
import com.houseclash.backend.domain.model.CardType
import com.houseclash.backend.domain.model.cardlogic.CardEffectResult
import com.houseclash.backend.infrastructure.web.task.TaskResponse
import com.houseclash.backend.infrastructure.web.task.toResponse
import com.houseclash.backend.infrastructure.web.user.UserResponse
import com.houseclash.backend.infrastructure.web.user.toResponse
import java.time.LocalDateTime

// --- REQUESTS ---

data class UseCardRequest(
    val targetUserId: Long? = null,
    val targetTaskId: Long? = null,
    val targetCategoryId: Long? = null
)

// --- RESPONSES ---

data class CardResponse(
    val id: Long,
    val userId: Long,
    val type: CardType,
    val acquiredAt: LocalDateTime
)

data class CardEffectResultResponse(
    val description: String,
    val updatedUsers: List<UserResponse>,
    val updatedTasks: List<TaskResponse>
)

// --- MAPPERS ---

fun Card.toResponse() = CardResponse(
    id = this.id!!,
    userId = this.userId,
    type = this.type,
    acquiredAt = this.acquiredAt
)

fun CardEffectResult.toResponse() = CardEffectResultResponse(
    description = this.description,
    updatedUsers = this.updatedUsers.map { it.toResponse() },
    updatedTasks = this.updatedTasks.map { it.toResponse() }
)
