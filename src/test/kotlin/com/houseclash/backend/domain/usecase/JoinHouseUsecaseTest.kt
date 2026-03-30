package com.houseclash.backend.domain.usecase

import com.houseclash.backend.helper.HouseRepositoryTester
import com.houseclash.backend.helper.PasswordEncoderTester
import com.houseclash.backend.helper.UserRepositoryTester
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class JoinHouseUsecaseTest {
    private val userRepository = UserRepositoryTester()
    private val houseRepository = HouseRepositoryTester()
    private val registerUsecase = RegisterUserUsecase(userRepository, PasswordEncoderTester())
    private val createHouseUsecase = CreateHouseUsecase(userRepository, houseRepository)
    private val usecase = JoinHouseUsecase(userRepository, houseRepository)

    @Test
    fun `should join house correctly`() {
        val user = registerUsecase.execute("Test", "test@email.com", "Password1")
        val house = createHouseUsecase.execute(user.id!!, "Pis de Gràcia")
        val user2 = registerUsecase.execute("Test2", "test2@email.com", "Password2")
        val newUser = usecase.execute(user2.id!!, house.inviteCode)
        assertEquals(house.id, newUser.houseId)
    }

    @Test
    fun `should throw because user already has house`() {
        val user = registerUsecase.execute("Test", "test@email.com", "Password1")
        val house = createHouseUsecase.execute(user.id!!, "Pis de Gràcia")
        val user2 = registerUsecase.execute("Test2", "test2@email.com", "Password2")
        createHouseUsecase.execute(user2.id!!, "Carrer Mallorca")
        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(user.id, house.inviteCode)
        }
    }

    @Test
    fun `should throw because house doesnt exist`() {
        val user = registerUsecase.execute("Test2", "test2@email.com", "Password2")
        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(user.id!!, "FAKEID")
        }
    }
}
