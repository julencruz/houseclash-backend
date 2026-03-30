package com.houseclash.backend.domain.model

import java.time.LocalDateTime

data class House(
    val id: Long? = null,
    val name: String,
    val description: String = "",
    val inviteCode: String,
    val createdBy: Long,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun create(createdBy: Long, name: String, description: String = ""): House {
            require(name.isNotBlank()) { "House name cannot be blank" }
            return House(
                name = name,
                inviteCode = generateInviteCode(),
                createdBy = createdBy,
                description = description
            )
        }

        private fun generateInviteCode(): String {
            val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
            return (1..6).map { chars.random() }.joinToString("")
        }
    }
}
