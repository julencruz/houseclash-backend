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
    private val usecase = UpdateCategoryUsecase(categoryRepository, userRepository, houseRepository)

    @Test
    fun `should rename category successfully`() {
        val user = TestDataFactory.createUser(userRepository)
        val house = TestDataFactory.createHouse(houseRepository, userRepository, user)
        require(house.id != null)
        require(user.id != null)
        val cat = TestDataFactory.createCategory(categoryRepository, house.id, name = "Old")
        require(cat.id != null)

        val result = usecase.execute(user.id, cat.id, "NewName")
        assertEquals("NewName", result.name)
    }

    @Test
    fun `should fail if new name already exists in house`() {
        val user = TestDataFactory.createUser(userRepository)
        val house = TestDataFactory.createHouse(houseRepository, userRepository, user)
        require(house.id != null)
        require(user.id != null)
        TestDataFactory.createCategory(categoryRepository, house.id, name = "Existent")
        val catToRename = TestDataFactory.createCategory(categoryRepository, house.id, name = "Original")
        require(catToRename.id != null)

        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(user.id, catToRename.id, "Existent")
        }
    }

    @Test
    fun `should fail if user is not the house captain`() {
        val owner = TestDataFactory.createUser(userRepository, username = "Owner", email = "o@m.com")
        val house = TestDataFactory.createHouse(houseRepository, userRepository, owner)
        require(house.id != null)
        val member = TestDataFactory.createUser(userRepository, username = "Member", email = "m@m.com")
        userRepository.save(member.joinHouse(house.id))
        require(member.id != null)
        val cat = TestDataFactory.createCategory(categoryRepository, house.id, name = "Category")
        require(cat.id != null)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(member.id, cat.id, "New Name")
        }
        assertEquals("Only the house captain can update categories.", exception.message)
    }
}
