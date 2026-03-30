package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.User
import com.houseclash.backend.domain.port.PasswordEncoder
import com.houseclash.backend.domain.port.UserRepository

class LoginUserUsecase (
    private val userRepository : UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    fun execute(email: String, password : String) : User {
        require(email.isNotBlank()) { "Email cannot be blank" }

        val user = userRepository.findByEmail(email)
        if (user == null || !passwordEncoder.matches(password, user.passwordHash)) {
            throw IllegalArgumentException("Invalid email or password")
        }

        return user
    }
}
