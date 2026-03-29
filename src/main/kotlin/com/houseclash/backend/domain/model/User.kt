package com.houseclash.backend.domain.model

import java.time.LocalDateTime

data class User(
    val id: Long? = null,
    val username: String,
    val email: String,
    val passwordHash: String,
    val kudosBalance: Int = 0,
    val houseId: Long? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)