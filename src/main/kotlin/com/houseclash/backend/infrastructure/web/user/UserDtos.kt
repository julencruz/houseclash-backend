package com.houseclash.backend.infrastructure.web.user

import com.houseclash.backend.domain.model.User
import java.time.LocalDateTime

data class RegisterRequest(
    val username: String,
    val email: String,
    val passwordRaw: String
)

data class LoginRequest(
    val email: String,
    val passwordRaw: String
)

data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val houseId: Long?,
    val kudosBalance: Int,
    val createdAt: LocalDateTime
)

fun User.toResponse() = UserResponse(
    id = this.id!!,
    username = this.username,
    email = this.email,
    houseId = this.houseId,
    kudosBalance = this.kudosBalance,
    createdAt = this.createdAt
)
