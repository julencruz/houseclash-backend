package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.TaskStatus
import com.houseclash.backend.helper.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CompleteTaskUsecaseTest {
    private val userRepository = UserRepositoryTester()
    private val houseRepository = HouseRepositoryTester()
    private val taskRepository = TaskRepositoryTester()
    private val activityLogRepository = ActivityLogRepositoryTester()
    private val assignUsecase = AssignTaskUsecase(userRepository, taskRepository, activityLogRepository)
    private val usecase = CompleteTaskUsecase(taskRepository, userRepository, activityLogRepository)

    private val user = TestDataFactory.createUser(userRepository)
    private val house = TestDataFactory.createHouse(houseRepository, userRepository, user)
    private val updatedUser = userRepository.findById(user.id!!)!!
    private val task = TestDataFactory.createTask(taskRepository, house.id!!)

    @Test
    fun `should complete task successfully`() {
        assignUsecase.execute(task.id!!, updatedUser.id!!)
        val completed = usecase.execute(task.id, updatedUser.id)
        assertEquals(TaskStatus.PENDING_REVIEW, completed.status)
        assertNotNull(completed.completedAt)
    }

    @Test
    fun `should throw when task not found`() {
        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(999L, updatedUser.id!!)
        }
    }

    @Test
    fun `should throw when task is not assigned`() {
        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(task.id!!, updatedUser.id!!)
        }
    }

    @Test
    fun `should resubmit a disputed task back to pending review`() {
        val disputedTask = taskRepository.save(
            task.copy(status = TaskStatus.DISPUTED, assignedTo = updatedUser.id)
        )
        val result = usecase.execute(disputedTask.id!!, updatedUser.id!!)
        assertEquals(TaskStatus.PENDING_REVIEW, result.status)
        assertNotNull(result.completedAt)
    }

    @Test
    fun `should throw when trying to complete an approved task`() {
        val approvedTask = taskRepository.save(
            task.copy(status = TaskStatus.APPROVED, assignedTo = updatedUser.id)
        )
        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(approvedTask.id!!, updatedUser.id!!)
        }
    }
}
