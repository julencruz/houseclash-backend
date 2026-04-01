package com.houseclash.backend.domain.usecase

import com.houseclash.backend.helper.CategoryRepositoryTester
import com.houseclash.backend.helper.HouseRepositoryTester
import com.houseclash.backend.helper.TestDataFactory
import com.houseclash.backend.helper.UserRepositoryTester
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CreateCategoryHouseUsecaseTest {
    private val houseRepository = HouseRepositoryTester()
    private val categoryRepository = CategoryRepositoryTester()
    private val userRepository = UserRepositoryTester()

    private val usecase = CreateCategoryHouseUsecase(
        houseRepository,
        categoryRepository,
        userRepository
    )

    @Test
    fun `should create category successfully when user belongs to house`() {
        val user = TestDataFactory.createUser(userRepository)
        val house = TestDataFactory.createHouse(houseRepository, userRepository, user)

        val result = usecase.execute(user.id!!, house.id!!, "Cuina")

        assertNotNull(result.id)
        assertEquals("Cuina", result.name)
        assertEquals(house.id, result.houseId)
    }

    @Test
    fun `should throw when user tries to create category in another house`() {
        val owner = TestDataFactory.createUser(userRepository)
        val house = TestDataFactory.createHouse(houseRepository, userRepository, owner)

        val hacker = TestDataFactory.createUser(userRepository, username = "Hacker", email = "h@mail.com")

        val exception = assertThrows<IllegalArgumentException> {
            usecase.execute(hacker.id!!, house.id!!, "Menjador Hacker")
        }
        assertEquals("User does not belong to this house", exception.message)
    }

    @Test
    fun `should throw when category with same name already exists in the house`() {
        val user = TestDataFactory.createUser(userRepository)
        val house = TestDataFactory.createHouse(houseRepository, userRepository, user)

        TestDataFactory.createCategory(categoryRepository, house.id!!, "Bany")

        val exception = assertThrows<IllegalArgumentException> {
            usecase.execute(user.id!!, house.id, "bany")
        }
        assertEquals("Category 'bany' already exists in this house", exception.message)
    }

    @Test
    fun `should throw when house does not exist`() {
        val user = TestDataFactory.createUser(userRepository)

        val exception = assertThrows<IllegalArgumentException> {
            usecase.execute(user.id!!, 999L, "Menjador")
        }
        assertEquals("House not found for id: 999", exception.message)
    }
}
