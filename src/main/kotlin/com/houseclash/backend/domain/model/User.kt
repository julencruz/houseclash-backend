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

    fun leaveHouse(houseId: Long): User {
        require(this.houseId == houseId) { "User is not in this house" }
        return this.copy(
            houseId = null,
            kudosBalance = 0
        )
    }

    fun addKudos(kudos: Int): User {
        require(kudos > 0) { "Kudos to add must be positive" }
        return this.copy(kudosBalance = this.kudosBalance + kudos)
    }

    fun spendKudos(amount: Int): User {
        require(amount > 0) { "Amount must be positive" }
        require(kudosBalance >= amount) { "Insufficient kudos balance" }
        return this.copy(kudosBalance = kudosBalance - amount)
    }

    fun penalizeKudos(amount: Int = 2): User {
        require(amount > 0) { "Penalty amount must be positive" }
        val newBalance = if (kudosBalance - amount < 0) 0 else kudosBalance - amount
        return this.copy(kudosBalance = newBalance)
    }

    fun update(
        newUsername: String? = null,
        newPasswordHash: String? = null
    ): User {
        newUsername?.let { require(it.isNotBlank()) { "Username cannot be blank" } }
        newPasswordHash?.let { require(it.isNotBlank()) { "Password hash cannot be blank" } }

        return this.copy(
            username = newUsername ?: this.username,
            passwordHash = newPasswordHash ?: this.passwordHash
        )
    }
}
