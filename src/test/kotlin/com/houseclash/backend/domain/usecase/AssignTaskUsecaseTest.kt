package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.TaskStatus
import com.houseclash.backend.helper.HouseRepositoryTester
import com.houseclash.backend.helper.TaskRepositoryTester
import com.houseclash.backend.helper.TestDataFactory
import com.houseclash.backend.helper.UserRepositoryTester
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AssignTaskUsecaseTest {
    private val userRepository = UserRepositoryTester()
    private val houseRepository = HouseRepositoryTester()
    private val taskRepository = TaskRepositoryTester()
    private val usecase = AssignTaskUsecase(userRepository, taskRepository)

    private val user = TestDataFactory.createUser(userRepository)
    private val house = TestDataFactory.createHouse(houseRepository, userRepository, user)
    private val updatedUser = userRepository.findById(user.id!!)!!
    private val task = TestDataFactory.createTask(taskRepository, house.id!!)

    @Test
    fun `should assign task successfully`() {
        val assigned = usecase.execute(task.id!!, updatedUser.id!!)
        assertEquals(updatedUser.id, assigned.assignedTo)
        assertEquals(TaskStatus.ASSIGNED, assigned.status)
    }

    @Test
    fun `should throw when task already assigned`() {
        usecase.execute(task.id!!, updatedUser.id!!)
        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(task.id, updatedUser.id)
        }
    }

    @Test
    fun `should throw when user not in house`() {
        val otherUser = TestDataFactory.createUser(userRepository, "Other", "other@email.com")
        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(task.id!!, otherUser.id!!)
        }
    }

    @Test
    fun `should throw when user not found`() {
        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(task.id!!, 999L)
        }
    }

    @Test
    fun `should throw when task not found`() {
        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(999L, updatedUser.id!!)
        }
    }
}
