package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.TaskStatus
import com.houseclash.backend.helper.HouseRepositoryTester
import com.houseclash.backend.helper.TaskRepositoryTester
import com.houseclash.backend.helper.TestDataFactory
import com.houseclash.backend.helper.UserRepositoryTester
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UnassignTaskUsecaseTest {
    private val userRepository = UserRepositoryTester()
    private val houseRepository = HouseRepositoryTester()
    private val taskRepository = TaskRepositoryTester()
    private val activityLogRepository = com.houseclash.backend.helper.ActivityLogRepositoryTester()
    private val usecase = UnassignTaskUsecase(taskRepository, userRepository, activityLogRepository)

    @Test
    fun `should unassign task and penalize user by 3 kudos`() {
        val user = TestDataFactory.createUser(userRepository, kudosBalance = 10)
        val house = TestDataFactory.createHouse(houseRepository, userRepository, user)

        val task = TestDataFactory.createTask(taskRepository, house.id!!)
        val assignedTask = taskRepository.save(
            task.copy(status = TaskStatus.ASSIGNED, assignedTo = user.id!!, isForced = false)
        )

        val result = usecase.execute(user.id, assignedTask.id!!)

        val finalUser = userRepository.findById(user.id)!!

        assertEquals(TaskStatus.OPEN, result.status)
        assertNull(result.assignedTo)
        assertEquals(7, finalUser.kudosBalance)
    }

    @Test
    fun `should throw when trying to unassign a forced task`() {
        val user = TestDataFactory.createUser(userRepository)
        val task = TestDataFactory.createTask(taskRepository, 99L)

        val forcedTask = taskRepository.save(
            task.copy(status = TaskStatus.ASSIGNED, assignedTo = user.id!!, isForced = true)
        )

        val exception = assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(user.id, forcedTask.id!!)
        }
        assertEquals("Cannot unassign forced task", exception.message)
    }

    @Test
    fun `should throw when user tries to unassign a task belonging to someone else`() {
        val owner = TestDataFactory.createUser(userRepository)
        val hacker = TestDataFactory.createUser(userRepository, username = "Hacker", email = "hacker@email.com")

        val task = taskRepository.save(
            TestDataFactory.createTask(taskRepository, 99L)
                .copy(status = TaskStatus.ASSIGNED, assignedTo = owner.id!!)
        )

        val exception = assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(hacker.id!!, task.id!!)
        }
        assertEquals("Cannot unassign task assigned to another user", exception.message)
    }

    @Test
    fun `should throw when task is not in ASSIGNED status`() {
        val user = TestDataFactory.createUser(userRepository)
        val openTask = TestDataFactory.createTask(taskRepository, 99L)

        val corruptedTask = taskRepository.save(openTask.copy(assignedTo = user.id!!))

        val exception = assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(user.id, corruptedTask.id!!)
        }
        assertEquals("Task is not currently assigned", exception.message)
    }
}
