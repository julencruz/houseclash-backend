package com.houseclash.backend.domain.usecase

import com.houseclash.backend.helper.HouseRepositoryTester
import com.houseclash.backend.helper.TestDataFactory
import com.houseclash.backend.helper.UserRepositoryTester
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UpdateHouseUsecaseTest {
    private val houseRepository = HouseRepositoryTester()
    private val userRepository = UserRepositoryTester()
    private val usecase = UpdateHouseUsecase(houseRepository, userRepository)

    @Test
    fun `should update house name`() {
        val user = TestDataFactory.createUser(userRepository)
        val house = TestDataFactory.createHouse(houseRepository, userRepository, user)

        val result = usecase.execute(user.id!!, house.id!!, "Nova Llar")
        assertEquals("Nova Llar", result.name)
    }

    @Test
    fun `should fail if user does not belong to the house`() {
        val owner = TestDataFactory.createUser(userRepository, username = "Owner", email = "o@m.com")
        val house = TestDataFactory.createHouse(houseRepository, userRepository, owner)

        val hacker = TestDataFactory.createUser(userRepository, username = "Hacker", email = "h@m.com")

        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(hacker.id!!, house.id!!, "Hackejat")
        }
    }
}
