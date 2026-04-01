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
    private val usecase = DeleteCategoryUsecase(categoryRepository, userRepository, taskRepository)

    @Test
    fun `should delete empty category`() {
        val user = TestDataFactory.createUser(userRepository)
        val house = TestDataFactory.createHouse(houseRepository, userRepository, user)
        val cat = TestDataFactory.createCategory(categoryRepository, house.id!!)

        usecase.execute(user.id!!, cat.id!!)
        assertNull(categoryRepository.findById(cat.id))
    }

    @Test
    fun `should block deletion if category has tasks`() {
        val user = TestDataFactory.createUser(userRepository)
        val house = TestDataFactory.createHouse(houseRepository, userRepository, user)
        val cat = TestDataFactory.createCategory(categoryRepository, house.id!!)
        TestDataFactory.createTask(taskRepository, house.id, categoryId = cat.id!!)

        assertThrows(IllegalArgumentException::class.java) { usecase.execute(user.id!!, cat.id) }
    }
}
