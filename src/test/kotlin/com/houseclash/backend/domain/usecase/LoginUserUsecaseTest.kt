package com.houseclash.backend.domain.usecase

import com.houseclash.backend.helper.PasswordEncoderTester
import com.houseclash.backend.helper.UserRepositoryTester
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class LoginUserUsecaseTest {

    private val userRepository = UserRepositoryTester()
    private val passwordEncoder = PasswordEncoderTester()
    private val registerUsecase = RegisterUserUsecase(userRepository, passwordEncoder)
    private val usecase = LoginUserUsecase(userRepository, passwordEncoder)

    @Test
    fun `should login successfully with correct credentials`() {
        val registeredUser = registerUsecase.execute("Test", "test@email.com", "Password1")
        val user = usecase.execute("test@email.com", "Password1")
        assertEquals(user, registeredUser)
    }

    @Test
    fun `should throw when blank email`() {
        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute("", "Password1")
        }
    }

    @Test
    fun `should throw when no user with that email exists`() {
        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute("test@gmail.com", "Password1")
        }
    }

    @Test
    fun `should throw with incorrect password`() {
        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute("test@email.com", "Password2")
        }
    }
}
