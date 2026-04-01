package com.houseclash.backend.domain.usecase

import com.houseclash.backend.helper.PasswordEncoderTester
import com.houseclash.backend.helper.TestDataFactory
import com.houseclash.backend.helper.UserRepositoryTester
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UpdateUserUsecaseTest {
    private val userRepository = UserRepositoryTester()
    private val passwordEncoder = PasswordEncoderTester()
    private val usecase = UpdateUserUsecase(userRepository, passwordEncoder)

    @Test
    fun `should update username and password correctly`() {
        val user = TestDataFactory.createUser(userRepository, passwordHash = "hashed_old")
        val result = usecase.execute(user.id!!, "NewName", "old", "new")

        assertEquals("NewName", result.username)
        assertEquals("hashed_new", result.passwordHash)
    }

    @Test
    fun `should fail if old password does not match`() {
        val user = TestDataFactory.createUser(userRepository, passwordHash = "hashed_real")
        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(user.id!!, newPassword = "new", oldPassword = "wrong")
        }
    }
}
