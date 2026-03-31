package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.TaskStatus
import com.houseclash.backend.helper.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CompleteTaskUsecaseTest {
    private val userRepository = UserRepositoryTester()
    private val houseRepository = HouseRepositoryTester()
    private val taskRepository = TaskRepositoryTester()
    private val assignUsecase = AssignTaskUsecase(userRepository, taskRepository)
    private val usecase = CompleteTaskUsecase(taskRepository)

    private val user = TestDataFactory.createUser(userRepository)
    private val house = TestDataFactory.createHouse(houseRepository, userRepository, user)
    private val updatedUser = userRepository.findById(user.id!!)!!
    private val task = TestDataFactory.createTask(taskRepository, house.id!!)

    @Test
    fun `should complete task successfully`() {
        assignUsecase.execute(task.id!!, updatedUser.id!!)
        val completed = usecase.execute(task.id)
        assertEquals(TaskStatus.PENDING_REVIEW, completed.status)
        assertNotNull(completed.completedAt)
    }

    @Test
    fun `should throw when task not found`() {
        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(999L)
        }
    }

    @Test
    fun `should throw when task is not assigned`() {
        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(task.id!!)
        }
    }
}
