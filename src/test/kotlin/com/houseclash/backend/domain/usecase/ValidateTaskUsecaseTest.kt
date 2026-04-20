package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.TaskStatus
import com.houseclash.backend.helper.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ValidateTaskUsecaseTest {
    private val userRepository = UserRepositoryTester()
    private val houseRepository = HouseRepositoryTester()
    private val taskRepository = TaskRepositoryTester()
    private val activityLogRepository = ActivityLogRepositoryTester()
    private val assignUsecase = AssignTaskUsecase(userRepository, taskRepository, activityLogRepository)
    private val completeUsecase = CompleteTaskUsecase(taskRepository, userRepository, activityLogRepository)
    private val usecase = ValidateTaskUsecase(taskRepository, userRepository, activityLogRepository)

    private val user = TestDataFactory.createUser(userRepository)
    private val house = TestDataFactory.createHouse(houseRepository, userRepository, user)
    private val updatedUser = userRepository.findById(user.id!!)!!
    private val validator = TestDataFactory.createUser(userRepository, "Validator", "validator@email.com")

    private val task = TestDataFactory.createTask(taskRepository, house.id!!)

    private fun prepareCompletedTask() {
        assignUsecase.execute(task.id!!, updatedUser.id!!)
        completeUsecase.execute(task.id, updatedUser.id)
    }

    @Test
    fun `should approve task and add kudos to assignee`() {
        prepareCompletedTask()
        val kudosBefore = updatedUser.kudosBalance

        usecase.execute(task.id!!, validator.id!!, ValidationDecision.APPROVE)

        val updatedAssignee = userRepository.findById(updatedUser.id!!)!!
        assertEquals(TaskStatus.APPROVED, taskRepository.findById(task.id)!!.status)
        assertEquals(kudosBefore + task.kudosValue, updatedAssignee.kudosBalance)
    }

    @Test
    fun `should give 1 kudo incentive to validator on approve`() {
        prepareCompletedTask()
        val validatorKudosBefore = validator.kudosBalance

        usecase.execute(task.id!!, validator.id!!, ValidationDecision.APPROVE)

        val updatedValidator = userRepository.findById(validator.id)!!
        assertEquals(validatorKudosBefore + ValidateTaskUsecase.INCENTIVE, updatedValidator.kudosBalance)
    }

    @Test
    fun `should not give kudo incentive to validator on dispute`() {
        prepareCompletedTask()
        val validatorKudosBefore = validator.kudosBalance

        usecase.execute(task.id!!, validator.id!!, ValidationDecision.DISPUTE)

        val updatedValidator = userRepository.findById(validator.id)!!
        assertEquals(validatorKudosBefore, updatedValidator.kudosBalance)
    }

    @Test
    fun `should dispute task`() {
        prepareCompletedTask()

        val disputed = usecase.execute(task.id!!, validator.id!!, ValidationDecision.DISPUTE)

        assertEquals(TaskStatus.DISPUTED, disputed.status)
    }

    @Test
    fun `should throw when task not found`() {
        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(999L, validator.id!!, ValidationDecision.APPROVE)
        }
    }

    @Test
    fun `should throw when validator is the assignee`() {
        prepareCompletedTask()

        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(task.id!!, updatedUser.id!!, ValidationDecision.APPROVE)
        }
    }

    @Test
    fun `should throw when task is not pending review`() {
        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(task.id!!, validator.id!!, ValidationDecision.APPROVE)
        }
    }
}
