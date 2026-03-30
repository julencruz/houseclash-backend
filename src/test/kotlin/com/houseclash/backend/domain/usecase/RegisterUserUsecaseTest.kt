package com.houseclash.backend.domain.usecase

import com.houseclash.backend.helper.PasswordEncoderTester
import com.houseclash.backend.helper.UserRepositoryTester
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class RegisterUserUsecaseTest {

    private val userRepository = UserRepositoryTester()
    private val passwordEncoder = PasswordEncoderTester()
    private val usecase = RegisterUserUsecase(userRepository, passwordEncoder)

    @Test
    fun `should register a new user successfully`() {
        val user = usecase.execute("Test", "test@email.com", "Password1")

        assertEquals("Test", user.username)
        assertEquals("test@email.com", user.email)
        assertNotNull(user.id)
    }

    @Test
    fun `should throw when email already exists`() {
        usecase.execute("Test", "test@email.com", "Password1")

        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute("Otro", "test@email.com", "Password3")
        }
    }
}
