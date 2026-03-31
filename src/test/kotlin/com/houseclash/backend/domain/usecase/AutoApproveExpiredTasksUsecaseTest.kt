package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.TaskStatus
import com.houseclash.backend.helper.HouseRepositoryTester
import com.houseclash.backend.helper.TaskRepositoryTester
import com.houseclash.backend.helper.TestDataFactory
import com.houseclash.backend.helper.UserRepositoryTester
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class AutoApproveExpiredTasksUsecaseTest {
    private val userRepository = UserRepositoryTester()
    private val houseRepository = HouseRepositoryTester()
    private val taskRepository = TaskRepositoryTester()
    private val usecase = AutoApproveExpiredTasksUsecase(taskRepository, userRepository)

    @Test
    fun `should auto approve expired tasks, reward worker, and penalize validators`() {
        val worker = TestDataFactory.createUser(userRepository, "Worker", "w@test.com")
        val validator = TestDataFactory.createUser(userRepository, "Validator", "v@test.com")

        val house = TestDataFactory.createHouse(houseRepository, userRepository, worker)
        val updatedWorker = userRepository.findById(worker.id!!)!!
        val updatedValidator = userRepository.save(validator.joinHouse(house.id!!).addKudos(5))

        val task = TestDataFactory.createTask(taskRepository, house.id)
        val expiredTask = taskRepository.save(
            task.copy(
                status = TaskStatus.PENDING_REVIEW,
                assignedTo = updatedWorker.id,
                completedAt = LocalDateTime.now().minusHours(25)
            )
        )

        usecase.execute(house.id)

        val finalWorker = userRepository.findById(updatedWorker.id!!)!!
        val finalValidator = userRepository.findById(updatedValidator.id!!)!!
        val finalTask = taskRepository.findById(expiredTask.id!!)!!

        assertEquals(TaskStatus.AUTO_APPROVED, finalTask.status)
        assertEquals(4, finalWorker.kudosBalance)
        assertEquals(3, finalValidator.kudosBalance)
    }

    @Test
    fun `should ignore tasks that are not yet expired`() {
        val worker = TestDataFactory.createUser(userRepository)
        val house = TestDataFactory.createHouse(houseRepository, userRepository, worker)
        val updatedWorker = userRepository.findById(worker.id!!)!!

        val task = TestDataFactory.createTask(taskRepository, house.id!!)
        val pendingTask = taskRepository.save(
            task.copy(
                status = TaskStatus.PENDING_REVIEW,
                assignedTo = updatedWorker.id,
                completedAt = LocalDateTime.now().minusHours(10)
            )
        )

        usecase.execute(house.id)

        val finalWorker = userRepository.findById(updatedWorker.id!!)!!
        val finalTask = taskRepository.findById(pendingTask.id!!)!!

        assertEquals(TaskStatus.PENDING_REVIEW, finalTask.status)
        assertEquals(0, finalWorker.kudosBalance)
    }

    @Test
    fun `should ignore tasks not in PENDING_REVIEW status`() {
        val worker = TestDataFactory.createUser(userRepository)
        val house = TestDataFactory.createHouse(houseRepository, userRepository, worker)
        val updatedWorker = userRepository.findById(worker.id!!)!!

        val task = TestDataFactory.createTask(taskRepository, house.id!!)
        val assignedTask = taskRepository.save(
            task.copy(
                status = TaskStatus.ASSIGNED,
                assignedTo = updatedWorker.id
            )
        )

        usecase.execute(house.id)

        val finalTask = taskRepository.findById(assignedTask.id!!)!!
        assertEquals(TaskStatus.ASSIGNED, finalTask.status)
    }
}
