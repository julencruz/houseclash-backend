package com.houseclash.backend.domain.usecase

import com.houseclash.backend.helper.HouseRepositoryTester
import com.houseclash.backend.helper.PasswordEncoderTester
import com.houseclash.backend.helper.UserRepositoryTester
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class CreateHouseUsecaseTest {
    private val userRepository = UserRepositoryTester()
    private val houseRepository = HouseRepositoryTester()
    private val registerUsecase = RegisterUserUsecase(userRepository, PasswordEncoderTester())
    private val usecase = CreateHouseUsecase(userRepository, houseRepository)

    @Test
    fun `should create house correctly`() {
        val user = registerUsecase.execute("Test", "test@email.com", "Password1")
        val house = usecase.execute(user.id!!, "Pis de Gràcia")
        assertEquals("Pis de Gràcia", house.name)
    }

    @Test
    fun `should create house with description`() {
        val user = registerUsecase.execute("Test", "test@email.com", "Password1")
        val house = usecase.execute(user.id!!, "Pis de Gràcia", "Pis de 3 habitacions compartit per Laia, Aina, Judith")
        assertEquals("Pis de Gràcia", house.name)
        assertEquals("Pis de 3 habitacions compartit per Laia, Aina, Judith", house.description)
    }

    @Test
    fun `should throw when user already member of a house`() {
        val user = registerUsecase.execute("Test", "test@email.com", "Password1")
        usecase.execute(user.id!!, "Pis de Gràcia", "Pis de 3 habitacions compartit per Laia, Aina, Judith")
        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(user.id, "Carrer Mallorca")
        }
    }
}
