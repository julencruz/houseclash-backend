package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.Effort
import com.houseclash.backend.domain.model.House
import com.houseclash.backend.helper.CategoryRepositoryTester
import com.houseclash.backend.helper.HouseRepositoryTester
import com.houseclash.backend.helper.TaskRepositoryTester
import com.houseclash.backend.helper.TestDataFactory
import com.houseclash.backend.helper.UserRepositoryTester
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UpdateTaskUsecaseTest {
    private val taskRepository = TaskRepositoryTester()
    private val userRepository = UserRepositoryTester()
    private val categoryRepository = CategoryRepositoryTester()
    private val houseRepository = HouseRepositoryTester()
    private val usecase = UpdateTaskUsecase(taskRepository, userRepository, categoryRepository)

    @Test
    fun `should update task and reset kudos if effort changes`() {
        val user = TestDataFactory.createUser(userRepository)
        val house = TestDataFactory.createHouse(houseRepository, userRepository, user)
        val task = TestDataFactory.createTask(taskRepository, house.id!!, effort = Effort.LOW) // baseKudos = 2

        val inflatedTask = taskRepository.save(task.applyMarketInflation(5))

        val result = usecase.execute(user.id!!, inflatedTask.id!!, effort = Effort.HIGH)

        assertEquals(Effort.HIGH, result.effort)
        assertEquals(8, result.kudosValue)
    }

    @Test
    fun `should fail if moving task to a category of another house`() {
        val user = TestDataFactory.createUser(userRepository)
        val house = TestDataFactory.createHouse(houseRepository, userRepository, user)
        val ownCategory = TestDataFactory.createCategory(categoryRepository, house.id!!)
        val task = TestDataFactory.createTask(taskRepository, house.id, categoryId = ownCategory.id!!)

        val otherHouse = houseRepository.save(House.create(999L, "Other"))
        val foreignCategory = TestDataFactory.createCategory(categoryRepository, otherHouse.id!!)

        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(user.id!!, task.id!!, categoryId = foreignCategory.id!!)
        }
    }
}
