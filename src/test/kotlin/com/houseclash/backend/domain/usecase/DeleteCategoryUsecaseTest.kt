package com.houseclash.backend.domain.usecase

import com.houseclash.backend.helper.CategoryRepositoryTester
import com.houseclash.backend.helper.HouseRepositoryTester
import com.houseclash.backend.helper.TaskRepositoryTester
import com.houseclash.backend.helper.TestDataFactory
import com.houseclash.backend.helper.UserRepositoryTester
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DeleteCategoryUsecaseTest {
    private val categoryRepository = CategoryRepositoryTester()
    private val userRepository = UserRepositoryTester()
    private val taskRepository = TaskRepositoryTester()
    private val houseRepository = HouseRepositoryTester()
    private val usecase = DeleteCategoryUsecase(categoryRepository, userRepository, taskRepository, houseRepository)

    @Test
    fun `should delete empty category`() {
        val user = TestDataFactory.createUser(userRepository)
        val house = TestDataFactory.createHouse(houseRepository, userRepository, user)
        require(house.id != null)
        require(user.id != null)
        val cat = TestDataFactory.createCategory(categoryRepository, house.id)
        require(cat.id != null)

        usecase.execute(user.id, cat.id)
        assertNull(categoryRepository.findById(cat.id))
    }

    @Test
    fun `should block deletion if category has tasks`() {
        val user = TestDataFactory.createUser(userRepository)
        val house = TestDataFactory.createHouse(houseRepository, userRepository, user)
        require(house.id != null)
        require(user.id != null)
        val cat = TestDataFactory.createCategory(categoryRepository, house.id)
        require(cat.id != null)
        TestDataFactory.createTask(taskRepository, house.id, categoryId = cat.id)

        assertThrows(IllegalArgumentException::class.java) { usecase.execute(user.id, cat.id) }
    }

    @Test
    fun `should fail if user is not the house captain`() {
        val owner = TestDataFactory.createUser(userRepository, username = "Owner", email = "o@m.com")
        val house = TestDataFactory.createHouse(houseRepository, userRepository, owner)
        require(house.id != null)
        val member = TestDataFactory.createUser(userRepository, username = "Member", email = "m@m.com")
        userRepository.save(member.joinHouse(house.id))
        require(member.id != null)
        val cat = TestDataFactory.createCategory(categoryRepository, house.id)
        require(cat.id != null)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(member.id, cat.id)
        }
        assertEquals("Only the house captain can delete categories.", exception.message)
    }
}
