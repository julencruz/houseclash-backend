package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.Effort
import com.houseclash.backend.domain.model.TaskStatus
import com.houseclash.backend.helper.HouseRepositoryTester
import com.houseclash.backend.helper.TaskRepositoryTester
import com.houseclash.backend.helper.TestDataFactory
import com.houseclash.backend.helper.UserRepositoryTester
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ApplyMarketInflationUsecaseTest {
    private val userRepository = UserRepositoryTester()
    private val houseRepository = HouseRepositoryTester()
    private val taskRepository = TaskRepositoryTester()
    private val activityLogRepository = com.houseclash.backend.helper.ActivityLogRepositoryTester()

    private val usecase = ApplyMarketInflationUsecase(taskRepository, activityLogRepository)

    @Test
    fun `should increase kudos of all open tasks in the specific house`() {
        val user = TestDataFactory.createUser(userRepository)
        val house = TestDataFactory.createHouse(houseRepository, userRepository, user)
        val task = TestDataFactory.createTask(taskRepository, house.id!!, "Limpiar cocina", Effort.MEDIUM)

        usecase.execute(house.id)

        val updatedTask = taskRepository.findById(task.id!!)!!
        assertEquals(6, updatedTask.kudosValue)
    }

    @Test
    fun `should NOT increase kudos of tasks that are already assigned`() {
        val user = TestDataFactory.createUser(userRepository)
        val house = TestDataFactory.createHouse(houseRepository, userRepository, user)

        val task = TestDataFactory.createTask(taskRepository, house.id!!, "Lavar platos", Effort.LOW)

        val updatedUser = userRepository.findById(user.id!!)!!
        val assignedTask = taskRepository.save(task.assignTaskToUser(updatedUser.id!!))

        usecase.execute(house.id)

        val taskAfterMarket = taskRepository.findById(assignedTask.id!!)!!
        assertEquals(2, taskAfterMarket.kudosValue)
        assertEquals(TaskStatus.ASSIGNED, taskAfterMarket.status)
    }

    @Test
    fun `should NOT affect tasks from a different house`() {
        val user1 = TestDataFactory.createUser(userRepository, "User1", "u1@test.com")
        val house1 = TestDataFactory.createHouse(houseRepository, userRepository, user1, "Casa 1")

        val user2 = TestDataFactory.createUser(userRepository, "User2", "u2@test.com")
        val house2 = TestDataFactory.createHouse(houseRepository, userRepository, user2, "Casa 2")

        val taskHouse1 = TestDataFactory.createTask(taskRepository, house1.id!!, "Tarea C1", Effort.HIGH)
        val taskHouse2 = TestDataFactory.createTask(taskRepository, house2.id!!, "Tarea C2", Effort.HIGH)

        usecase.execute(house1.id)

        assertEquals(11, taskRepository.findById(taskHouse1.id!!)!!.kudosValue)
        assertEquals(8, taskRepository.findById(taskHouse2.id!!)!!.kudosValue)
    }
}
