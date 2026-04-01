package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.User
import com.houseclash.backend.domain.port.PasswordEncoder
import com.houseclash.backend.domain.port.UserRepository

class UpdateUserUsecase(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    fun execute(
        userId: Long,
        username: String? = null,
        oldPassword: String? = null,
        newPassword: String? = null
    ): User {
        val user = userRepository.findById(userId)
        requireNotNull(user) { "User not found" }

        var finalPasswordHash = user.passwordHash

        if (!newPassword.isNullOrBlank()) {
            require(!oldPassword.isNullOrBlank()) { "Old password is required to set a new password" }
            require(passwordEncoder.matches(oldPassword, user.passwordHash)) { "Old password does not match" }

            finalPasswordHash = passwordEncoder.encode(newPassword)
        }

        val updatedUser = user.update(
            newUsername = username ?: user.username,
            newPasswordHash = finalPasswordHash
        )

        return userRepository.save(updatedUser)
    }
}
