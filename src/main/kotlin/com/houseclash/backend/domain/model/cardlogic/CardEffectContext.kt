package com.houseclash.backend.domain.model.cardlogic

import com.houseclash.backend.domain.model.Task
import com.houseclash.backend.domain.model.User

data class CardEffectContext(
    val executingUser: User,
    val houseId: Long,
    val targetUser: User? = null,
    val targetCategoryId: Long? = null,
    val targetTask: Task? = null,
    val houseMembers: List<User> = emptyList(),
    val houseTasks: List<Task> = emptyList(),

)
