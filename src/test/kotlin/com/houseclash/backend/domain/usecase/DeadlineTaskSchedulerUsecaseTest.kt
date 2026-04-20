package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.Effort
import com.houseclash.backend.domain.model.Task
import com.houseclash.backend.domain.model.TaskStatus
import com.houseclash.backend.helper.HouseRepositoryTester
import com.houseclash.backend.helper.TaskRepositoryTester
import com.houseclash.backend.helper.TestDataFactory
import com.houseclash.backend.helper.UserRepositoryTester
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class DeadlineTaskSchedulerUsecaseTest {
    private val FIXED_NOW = LocalDateTime.of(2026, 1, 15, 12, 0, 0)

    private val userRepository = UserRepositoryTester()
    private val houseRepository = HouseRepositoryTester()
    private val taskRepository = TaskRepositoryTester()
    private val usecase = RecurringTaskSchedulerUsecase(taskRepository) { FIXED_NOW }

    private val user = TestDataFactory.createUser(userRepository)
    private val house = TestDataFactory.createHouse(houseRepository, userRepository, user)

    @Test
    fun `scheduler should not touch assigned task with expired deadline`() {
        val updatedUser = userRepository.findById(user.id!!)!!
        val task = taskRepository.save(
            Task.create("Canviar llençols", null, Effort.MEDIUM, null, FIXED_NOW.minusHours(1), house.id!!, 1L)
                .copy(status = TaskStatus.ASSIGNED, assignedTo = updatedUser.id)
        )

        usecase.execute()

        // Scheduler does nothing with deadlines — kudos check happens at approval time
        val updated = taskRepository.findById(task.id!!)!!
        assertEquals(TaskStatus.ASSIGNED, updated.status)
        assertNotNull(updated.deadline)
        assertEquals(updatedUser.kudosBalance, userRepository.findById(updatedUser.id!!)!!.kudosBalance)
    }

    @Test
    fun `should give 0 kudos when approving task completed after deadline`() {
        val activityLogRepository = com.houseclash.backend.helper.ActivityLogRepositoryTester()
        val assignUsecase = AssignTaskUsecase(userRepository, taskRepository, activityLogRepository)
        val validateUsecase = ValidateTaskUsecase(taskRepository, userRepository, activityLogRepository)
        val validator = TestDataFactory.createUser(userRepository, "Validator", "v@test.com")
        val updatedUser = userRepository.findById(user.id!!)!!
        val kudosBefore = updatedUser.kudosBalance

        val task = taskRepository.save(
            Task.create("Canviar llençols", null, Effort.MEDIUM, null, FIXED_NOW.minusDays(1), house.id!!, 1L)
        )
        assignUsecase.execute(task.id!!, updatedUser.id!!)

        // Simulate completing after deadline
        taskRepository.save(
            taskRepository.findById(task.id)!!.copy(
                status = TaskStatus.PENDING_REVIEW,
                completedAt = FIXED_NOW // after deadline (FIXED_NOW.minusDays(1))
            )
        )

        validateUsecase.execute(task.id, validator.id!!, ValidationDecision.APPROVE)

        val finalUser = userRepository.findById(updatedUser.id)!!
        assertEquals(kudosBefore, finalUser.kudosBalance) // 0 kudos gained
    }

    @Test
    fun `should give normal kudos when approving task completed before deadline`() {
        val activityLogRepository = com.houseclash.backend.helper.ActivityLogRepositoryTester()
        val assignUsecase = AssignTaskUsecase(userRepository, taskRepository, activityLogRepository)
        val validateUsecase = ValidateTaskUsecase(taskRepository, userRepository, activityLogRepository)
        val validator = TestDataFactory.createUser(userRepository, "Validator", "v@test.com")
        val updatedUser = userRepository.findById(user.id!!)!!
        val kudosBefore = updatedUser.kudosBalance

        val task = taskRepository.save(
            Task.create("Canviar llençols", null, Effort.MEDIUM, null, FIXED_NOW.plusDays(1), house.id!!, 1L)
        )
        assignUsecase.execute(task.id!!, updatedUser.id!!)

        // Simulate completing before deadline
        taskRepository.save(
            taskRepository.findById(task.id)!!.copy(
                status = TaskStatus.PENDING_REVIEW,
                completedAt = FIXED_NOW.minusHours(1) // before deadline (FIXED_NOW.plusDays(1))
            )
        )

        validateUsecase.execute(task.id, validator.id!!, ValidationDecision.APPROVE)

        val finalUser = userRepository.findById(updatedUser.id)!!
        assertEquals(kudosBefore + task.kudosValue, finalUser.kudosBalance)
    }
}
