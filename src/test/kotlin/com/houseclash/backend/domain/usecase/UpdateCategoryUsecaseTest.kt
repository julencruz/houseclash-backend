package com.houseclash.backend.domain.usecase

import com.houseclash.backend.helper.CategoryRepositoryTester
import com.houseclash.backend.helper.HouseRepositoryTester
import com.houseclash.backend.helper.TestDataFactory
import com.houseclash.backend.helper.UserRepositoryTester
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UpdateCategoryUsecaseTest {
    private val categoryRepository = CategoryRepositoryTester()
    private val userRepository = UserRepositoryTester()
    private val houseRepository = HouseRepositoryTester()
    private val usecase = UpdateCategoryUsecase(categoryRepository, userRepository)

    @Test
    fun `should rename category successfully`() {
        val user = TestDataFactory.createUser(userRepository)
        val house = TestDataFactory.createHouse(houseRepository, userRepository, user)
        val cat = TestDataFactory.createCategory(categoryRepository, house.id!!, name = "Old")

        val result = usecase.execute(user.id!!, cat.id!!, "NewName")
        assertEquals("NewName", result.name)
    }

    @Test
    fun `should fail if new name already exists in house`() {
        val user = TestDataFactory.createUser(userRepository)
        val house = TestDataFactory.createHouse(houseRepository, userRepository, user)
        TestDataFactory.createCategory(categoryRepository, house.id!!, name = "Existent")
        val catToRename = TestDataFactory.createCategory(categoryRepository, house.id, name = "Original")

        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(user.id!!, catToRename.id!!, "Existent")
        }
    }
}
