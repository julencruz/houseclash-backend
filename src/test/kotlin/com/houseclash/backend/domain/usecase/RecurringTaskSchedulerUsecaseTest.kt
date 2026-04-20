package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.Effort
import com.houseclash.backend.domain.model.Recurrence
import com.houseclash.backend.domain.model.Task
import com.houseclash.backend.domain.model.TaskStatus
import com.houseclash.backend.helper.HouseRepositoryTester
import com.houseclash.backend.helper.TaskRepositoryTester
import com.houseclash.backend.helper.TestDataFactory
import com.houseclash.backend.helper.UserRepositoryTester
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class RecurringTaskSchedulerUsecaseTest {
    private val FIXED_NOW = LocalDateTime.of(2026, 1, 15, 12, 0, 0)

    private val userRepository = UserRepositoryTester()
    private val houseRepository = HouseRepositoryTester()
    private val taskRepository = TaskRepositoryTester()
    private val usecase = RecurringTaskSchedulerUsecase(taskRepository) { FIXED_NOW }

    private val user = TestDataFactory.createUser(userRepository)
    private val house = TestDataFactory.createHouse(houseRepository, userRepository, user)

    @Test
    fun `should reset approved recurring task`() {
        val task = taskRepository.save(
            Task.create("Treure escombraries", null, Effort.LOW, Recurrence.WEEKLY, null, house.id!!, 1L)
                .copy(status = TaskStatus.APPROVED, completedAt = FIXED_NOW.minusDays(8), createdAt = FIXED_NOW.minusDays(8))
        )

        usecase.execute()

        val updated = taskRepository.findById(task.id!!)!!
        assertEquals(TaskStatus.OPEN, updated.status)
        assertNull(updated.assignedTo)
        assertNull(updated.completedAt)
        assertEquals(Effort.LOW.baseKudos, updated.kudosValue)
    }

    @Test
    fun `should not reset task that is not due yet`() {
        val task = taskRepository.save(
            Task.create("Treure escombraries", null, Effort.LOW, Recurrence.WEEKLY, null, house.id!!, 1L)
                .copy(status = TaskStatus.APPROVED, completedAt = FIXED_NOW.minusDays(1), createdAt = FIXED_NOW.minusDays(1))
        )

        usecase.execute()

        assertEquals(TaskStatus.APPROVED, taskRepository.findById(task.id!!)!!.status)
    }

    @Test
    fun `should not reset non recurring task`() {
        val task = taskRepository.save(
            Task.create("Treure escombraries", null, Effort.LOW, null, null, house.id!!, 1L)
                .copy(status = TaskStatus.APPROVED)
        )

        usecase.execute()

        assertEquals(TaskStatus.APPROVED, taskRepository.findById(task.id!!)!!.status)
    }

    @Test
    fun `should not reset task still in progress`() {
        val task = taskRepository.save(
            Task.create("Treure escombraries", null, Effort.LOW, Recurrence.WEEKLY, null, house.id!!, 1L)
                .copy(status = TaskStatus.ASSIGNED)
        )

        usecase.execute()

        assertEquals(TaskStatus.ASSIGNED, taskRepository.findById(task.id!!)!!.status)
    }

    @Test
    fun `should reset disputed recurring task`() {
        val task = taskRepository.save(
            Task.create("Treure escombraries", null, Effort.LOW, Recurrence.WEEKLY, null, house.id!!, 1L)
                .copy(status = TaskStatus.DISPUTED, completedAt = FIXED_NOW.minusDays(8), createdAt = FIXED_NOW.minusDays(8))
        )

        usecase.execute()

        assertEquals(TaskStatus.OPEN, taskRepository.findById(task.id!!)!!.status)
    }

    @Test
    fun `should unassign assigned task that missed its cycle without penalty`() {
        val updatedUser = userRepository.findById(user.id!!)!!
        val kudosBefore = updatedUser.kudosBalance
        val task = taskRepository.save(
            Task.create("Treure escombraries", null, Effort.LOW, Recurrence.WEEKLY, null, house.id!!, 1L)
                .copy(status = TaskStatus.ASSIGNED, assignedTo = updatedUser.id, createdAt = FIXED_NOW.minusDays(8))
        )

        usecase.execute()

        val updatedTask = taskRepository.findById(task.id!!)!!
        assertEquals(TaskStatus.OPEN, updatedTask.status)
        assertNull(updatedTask.assignedTo)
        assertEquals(kudosBefore, userRepository.findById(updatedUser.id!!)!!.kudosBalance)
    }

    @Test
    fun `should unassign pending review task that missed its cycle without penalty`() {
        val updatedUser = userRepository.findById(user.id!!)!!
        val kudosBefore = updatedUser.kudosBalance
        val task = taskRepository.save(
            Task.create("Treure escombraries", null, Effort.MEDIUM, Recurrence.WEEKLY, null, house.id!!, 1L)
                .copy(status = TaskStatus.PENDING_REVIEW, assignedTo = updatedUser.id, createdAt = FIXED_NOW.minusDays(8), completedAt = FIXED_NOW.minusDays(2))
        )

        usecase.execute()

        val updatedTask = taskRepository.findById(task.id!!)!!
        assertEquals(TaskStatus.OPEN, updatedTask.status)
        assertNull(updatedTask.assignedTo)
        assertEquals(kudosBefore, userRepository.findById(updatedUser.id!!)!!.kudosBalance)
    }

    @Test
    fun `should not unassign task that has not yet missed its cycle`() {
        val task = taskRepository.save(
            Task.create("Treure escombraries", null, Effort.LOW, Recurrence.WEEKLY, null, house.id!!, 1L)
                .copy(status = TaskStatus.ASSIGNED, assignedTo = user.id, createdAt = FIXED_NOW.minusDays(3))
        )

        usecase.execute()

        val updated = taskRepository.findById(task.id!!)!!
        assertEquals(TaskStatus.ASSIGNED, updated.status)
        assertNotNull(updated.assignedTo)
    }

    @Test
    fun `should recalculate deadline when resetting recurring task`() {
        val originalDeadline = FIXED_NOW.minusDays(1)
        val task = taskRepository.save(
            Task.create("Treure escombraries", null, Effort.LOW, Recurrence.WEEKLY, null, house.id!!, 1L)
                .copy(status = TaskStatus.APPROVED, completedAt = FIXED_NOW.minusDays(8), createdAt = FIXED_NOW.minusDays(8), deadline = originalDeadline)
        )

        usecase.execute()

        val updated = taskRepository.findById(task.id!!)!!
        assertEquals(TaskStatus.OPEN, updated.status)
        assertEquals(originalDeadline.plusWeeks(1), updated.deadline)
    }
}
