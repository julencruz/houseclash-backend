package com.houseclash.backend.domain.model.cardlogic

import com.houseclash.backend.domain.model.Task
import com.houseclash.backend.domain.model.User

data class CardEffectResult (
    val updatedUsers: List<User> = emptyList(),
    val updatedTask: Task? = null,
    val description: String = ""
)
