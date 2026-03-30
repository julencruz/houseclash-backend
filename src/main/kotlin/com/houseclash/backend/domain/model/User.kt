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
) {
    companion object {
        fun create(username: String, email: String, encodedPassword: String): User {
            require(username.isNotBlank()) { "Username must not be blank" }
            require(email.isNotBlank()) { "Email must not be blank" }
            require(email.contains("@")) { "Invalid email format" }
            return User(
                username = username,
                email = email,
                passwordHash = encodedPassword,
            )
        }
    }

    fun joinHouse(houseId: Long): User {
        require(this.houseId == null) { "User already belongs to a house" }
        return this.copy(houseId = houseId)
    }
}
