package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.User
import com.houseclash.backend.domain.port.PasswordEncoder
import com.houseclash.backend.domain.port.UserRepository

class RegisterUserUsecase (
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    fun execute(username: String, email: String, rawPassword: String) : User {
        if (userRepository.findByEmail(email) != null) {
            throw IllegalArgumentException("Email already exists")
        }
        val encodedPassword = passwordEncoder.encode(rawPassword)
        val newUser = User.create(username, email, encodedPassword)
        return userRepository.save(newUser)
    }
}