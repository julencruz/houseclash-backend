package com.houseclash.backend.domain.model

import java.time.LocalDateTime

data class House(
    val id: Long? = null,
    val name: String,
    val description: String,
    val inviteCode: String,
    val createdBy: Long,
    val createdAt: LocalDateTime,
)