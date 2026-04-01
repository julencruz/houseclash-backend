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
    private val usecase = RecurringTaskSchedulerUsecase(taskRepository, FIXED_NOW)

    private val user = TestDataFactory.createUser(userRepository)
    private val house = TestDataFactory.createHouse(houseRepository, userRepository, user)

    @Test
    fun `should reset approved recurring task`() {
        val task = taskRepository.save(
            Task.create("Tirar basura", null, Effort.LOW, Recurrence.WEEKLY, house.id!!, 1L)
                .copy(
                    status = TaskStatus.APPROVED,
                    completedAt = FIXED_NOW.minusDays(8),
                    createdAt = FIXED_NOW.minusDays(8)
                )
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
            Task.create("Tirar basura", null, Effort.LOW, Recurrence.WEEKLY, house.id!!, 1L)
                .copy(
                    status = TaskStatus.APPROVED,
                    completedAt = FIXED_NOW.minusDays(1),
                    createdAt = FIXED_NOW.minusDays(1)
                )
        )

        usecase.execute()

        val updated = taskRepository.findById(task.id!!)!!
        assertEquals(TaskStatus.APPROVED, updated.status)
    }

    @Test
    fun `should not reset non recurring task`() {
        val task = taskRepository.save(
            Task.create("Tirar basura", null, Effort.LOW, null, house.id!!, 1L)
                .copy(status = TaskStatus.APPROVED)
        )

        usecase.execute()

        val updated = taskRepository.findById(task.id!!)!!
        assertEquals(TaskStatus.APPROVED, updated.status)
    }

    @Test
    fun `should not reset task still in progress`() {
        val task = taskRepository.save(
            Task.create("Tirar basura", null, Effort.LOW, Recurrence.WEEKLY, house.id!!, 1L)
                .copy(status = TaskStatus.ASSIGNED)
        )

        usecase.execute()

        val updated = taskRepository.findById(task.id!!)!!
        assertEquals(TaskStatus.ASSIGNED, updated.status)
    }

    @Test
    fun `should reset disputed recurring task`() {
        val task = taskRepository.save(
            Task.create("Tirar basura", null, Effort.LOW, Recurrence.WEEKLY, house.id!!, 1L)
                .copy(
                    status = TaskStatus.DISPUTED,
                    completedAt = FIXED_NOW.minusDays(8),
                    createdAt = FIXED_NOW.minusDays(8)
                )
        )

        usecase.execute()

        val updated = taskRepository.findById(task.id!!)!!
        assertEquals(TaskStatus.OPEN, updated.status)
    }
}
