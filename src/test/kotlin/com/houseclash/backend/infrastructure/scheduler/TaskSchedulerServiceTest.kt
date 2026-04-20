package com.houseclash.backend.infrastructure.scheduler

import com.houseclash.backend.domain.model.Effort
import com.houseclash.backend.domain.model.Recurrence
import com.houseclash.backend.domain.model.Task
import com.houseclash.backend.domain.model.TaskStatus
import com.houseclash.backend.domain.usecase.ApplyMarketInflationUsecase
import com.houseclash.backend.domain.usecase.AutoApproveExpiredTasksUsecase
import com.houseclash.backend.domain.usecase.PurgeActivityLogUsecase
import com.houseclash.backend.domain.usecase.RecurringTaskSchedulerUsecase
import com.houseclash.backend.helper.HouseRepositoryTester
import com.houseclash.backend.helper.TaskRepositoryTester
import com.houseclash.backend.helper.TestDataFactory
import com.houseclash.backend.helper.UserRepositoryTester
import com.houseclash.backend.helper.ActivityLogRepositoryTester
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class TaskSchedulerServiceTest {

    private val FIXED_NOW = LocalDateTime.of(2026, 1, 15, 12, 0, 0)

    private val userRepository = UserRepositoryTester()
    private val houseRepository = HouseRepositoryTester()
    private val taskRepository = TaskRepositoryTester()
    private val activityLogRepository = ActivityLogRepositoryTester()

    private val recurringTaskSchedulerUsecase = RecurringTaskSchedulerUsecase(taskRepository) { FIXED_NOW }
    private val autoApproveExpiredTasksUsecase = AutoApproveExpiredTasksUsecase(taskRepository, userRepository, activityLogRepository)
    private val applyMarketInflationUsecase = ApplyMarketInflationUsecase(taskRepository, activityLogRepository)
    private val purgeActivityLogUsecase = PurgeActivityLogUsecase(activityLogRepository, taskRepository)

    private val scheduler = TaskSchedulerService(
        recurringTaskSchedulerUsecase,
        autoApproveExpiredTasksUsecase,
        applyMarketInflationUsecase,
        purgeActivityLogUsecase,
        houseRepository
    )

    // ---- scheduleRecurringTasks ----

    @Test
    fun `scheduleRecurringTasks should reset recurring tasks due in all houses`() {
        val user1 = TestDataFactory.createUser(userRepository, "User1", "u1@test.com")
        val house1 = TestDataFactory.createHouse(houseRepository, userRepository, user1, "Casa 1")
        val user2 = TestDataFactory.createUser(userRepository, "User2", "u2@test.com")
        val house2 = TestDataFactory.createHouse(houseRepository, userRepository, user2, "Casa 2")

        val taskHouse1 = taskRepository.save(
            Task.create("Tarea C1", null, Effort.LOW, Recurrence.WEEKLY, null, house1.id!!, 1L)
                .copy(status = TaskStatus.APPROVED, completedAt = FIXED_NOW.minusDays(8), createdAt = FIXED_NOW.minusDays(8))
        )
        val taskHouse2 = taskRepository.save(
            Task.create("Tarea C2", null, Effort.MEDIUM, Recurrence.WEEKLY, null, house2.id!!, 1L)
                .copy(status = TaskStatus.APPROVED, completedAt = FIXED_NOW.minusDays(8), createdAt = FIXED_NOW.minusDays(8))
        )

        scheduler.scheduleRecurringTasks()

        assertEquals(TaskStatus.OPEN, taskRepository.findById(taskHouse1.id!!)!!.status)
        assertEquals(TaskStatus.OPEN, taskRepository.findById(taskHouse2.id!!)!!.status)
    }

    @Test
    fun `scheduleRecurringTasks should not reset tasks that are not due yet`() {
        val user = TestDataFactory.createUser(userRepository)
        val house = TestDataFactory.createHouse(houseRepository, userRepository, user)

        val task = taskRepository.save(
            Task.create("Tarea recurrente", null, Effort.LOW, Recurrence.WEEKLY, null, house.id!!, 1L)
                .copy(status = TaskStatus.APPROVED, completedAt = FIXED_NOW.minusDays(1), createdAt = FIXED_NOW.minusDays(1))
        )

        scheduler.scheduleRecurringTasks()

        assertEquals(TaskStatus.APPROVED, taskRepository.findById(task.id!!)!!.status)
    }

    @Test
    fun `scheduleRecurringTasks should reset kudos to base value on reset`() {
        val user = TestDataFactory.createUser(userRepository)
        val house = TestDataFactory.createHouse(houseRepository, userRepository, user)

        val task = taskRepository.save(
            Task.create("Tarea recurrente", null, Effort.MEDIUM, Recurrence.WEEKLY, null, house.id!!, 1L)
                .copy(status = TaskStatus.APPROVED, completedAt = FIXED_NOW.minusDays(8), createdAt = FIXED_NOW.minusDays(8), kudosValue = 20)
        )

        scheduler.scheduleRecurringTasks()

        val updated = taskRepository.findById(task.id!!)!!
        assertEquals(Effort.MEDIUM.baseKudos, updated.kudosValue)
        assertNull(updated.assignedTo)
        assertNull(updated.completedAt)
    }

    // ---- autoApproveExpiredTasks ----

    @Test
    fun `autoApproveExpiredTasks should auto-approve expired tasks in all houses`() {
        val user1 = TestDataFactory.createUser(userRepository, "User1", "u1@test.com")
        val house1 = TestDataFactory.createHouse(houseRepository, userRepository, user1, "Casa 1")
        val user2 = TestDataFactory.createUser(userRepository, "User2", "u2@test.com")
        val house2 = TestDataFactory.createHouse(houseRepository, userRepository, user2, "Casa 2")

        val taskHouse1 = taskRepository.save(
            Task.create("Tarea C1", null, Effort.LOW, null, null, house1.id!!, 1L)
                .copy(status = TaskStatus.PENDING_REVIEW, assignedTo = user1.id, completedAt = LocalDateTime.now().minusHours(73))
        )
        val taskHouse2 = taskRepository.save(
            Task.create("Tarea C2", null, Effort.LOW, null, null, house2.id!!, 1L)
                .copy(status = TaskStatus.PENDING_REVIEW, assignedTo = user2.id, completedAt = LocalDateTime.now().minusHours(73))
        )

        scheduler.autoApproveExpiredTasks()

        assertEquals(TaskStatus.AUTO_APPROVED, taskRepository.findById(taskHouse1.id!!)!!.status)
        assertEquals(TaskStatus.AUTO_APPROVED, taskRepository.findById(taskHouse2.id!!)!!.status)
    }

    @Test
    fun `autoApproveExpiredTasks should not auto-approve tasks not yet expired`() {
        val user = TestDataFactory.createUser(userRepository)
        val house = TestDataFactory.createHouse(houseRepository, userRepository, user)

        val task = taskRepository.save(
            Task.create("Tarea pendiente", null, Effort.LOW, null, null, house.id!!, 1L)
                .copy(status = TaskStatus.PENDING_REVIEW, assignedTo = user.id, completedAt = LocalDateTime.now().minusHours(1))
        )

        scheduler.autoApproveExpiredTasks()

        assertEquals(TaskStatus.PENDING_REVIEW, taskRepository.findById(task.id!!)!!.status)
    }

    @Test
    fun `autoApproveExpiredTasks should add kudos to the assignee`() {
        val user = TestDataFactory.createUser(userRepository)
        val house = TestDataFactory.createHouse(houseRepository, userRepository, user)
        val kudosBefore = userRepository.findById(user.id!!)!!.kudosBalance

        val task = taskRepository.save(
            Task.create("Tarea", null, Effort.MEDIUM, null, null, house.id!!, 1L)
                .copy(status = TaskStatus.PENDING_REVIEW, assignedTo = user.id, completedAt = LocalDateTime.now().minusHours(73))
        )

        scheduler.autoApproveExpiredTasks()

        val updatedUser = userRepository.findById(user.id)!!
        assertEquals(kudosBefore + task.kudosValue, updatedUser.kudosBalance)
    }

    // ---- applyMarketInflation ----

    @Test
    fun `applyMarketInflation should increase kudos of open tasks in all houses`() {
        val user1 = TestDataFactory.createUser(userRepository, "User1", "u1@test.com")
        val house1 = TestDataFactory.createHouse(houseRepository, userRepository, user1, "Casa 1")
        val user2 = TestDataFactory.createUser(userRepository, "User2", "u2@test.com")
        val house2 = TestDataFactory.createHouse(houseRepository, userRepository, user2, "Casa 2")

        val taskHouse1 = TestDataFactory.createTask(taskRepository, house1.id!!, "Tarea C1", Effort.LOW)
        val taskHouse2 = TestDataFactory.createTask(taskRepository, house2.id!!, "Tarea C2", Effort.HIGH)

        scheduler.applyMarketInflation()

        assertEquals(Effort.LOW.baseKudos + 1, taskRepository.findById(taskHouse1.id!!)!!.kudosValue)
        assertEquals(Effort.HIGH.baseKudos + 3, taskRepository.findById(taskHouse2.id!!)!!.kudosValue)
    }

    @Test
    fun `applyMarketInflation should not affect assigned tasks`() {
        val user = TestDataFactory.createUser(userRepository)
        val house = TestDataFactory.createHouse(houseRepository, userRepository, user)

        val task = taskRepository.save(
            TestDataFactory.createTask(taskRepository, house.id!!, "Tarea asignada", Effort.MEDIUM)
                .copy(status = TaskStatus.ASSIGNED, assignedTo = user.id)
        )
        val kudosBefore = task.kudosValue

        scheduler.applyMarketInflation()

        assertEquals(kudosBefore, taskRepository.findById(task.id!!)!!.kudosValue)
    }

    @Test
    fun `applyMarketInflation should not affect tasks when there are no houses`() {
        val task = taskRepository.save(
            Task.create("Tarea huerfana", null, Effort.LOW, null, null, 999L, 1L)
        )
        val kudosBefore = task.kudosValue

        scheduler.applyMarketInflation()

        assertEquals(kudosBefore, taskRepository.findById(task.id!!)!!.kudosValue)
    }
}
