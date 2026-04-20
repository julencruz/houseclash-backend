package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.*
import com.houseclash.backend.helper.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class ExecuteCardEffectUsecaseTest {
    private val userRepository = UserRepositoryTester()
    private val houseRepository = HouseRepositoryTester()
    private val taskRepository = TaskRepositoryTester()
    private val cardRepository = CardRepositoryTester()
    private val categoryRepository = CategoryRepositoryTester()
    private val activityLogRepository = ActivityLogRepositoryTester()
    private val usecase = ExecuteCardEffectUsecase(cardRepository, userRepository, taskRepository, activityLogRepository)

    private val executor = TestDataFactory.createUser(userRepository, "Executor", "executor@email.com", kudosBalance = 50)
    private val target = TestDataFactory.createUser(userRepository, "Target", "target@email.com", kudosBalance = 20)
    private val house = TestDataFactory.createHouse(houseRepository, userRepository, executor)
    private val updatedExecutor = userRepository.findById(executor.id!!)!!

    private val updatedTarget = userRepository.save(target.joinHouse(house.id!!))

    private fun saveCard(type: CardType): Card =
        cardRepository.save(Card.create(updatedExecutor.id!!, type))

    // --- HOUSE_BONUS ---

    @Test
    fun `HOUSE_BONUS should give +2 kudos to all members`() {
        val card = saveCard(CardType.HOUSE_BONUS)
        usecase.execute(card.id!!, updatedExecutor.id!!)

        val executorAfter = userRepository.findById(updatedExecutor.id)!!
        val targetAfter = userRepository.findById(updatedTarget.id!!)!!
        assertEquals(52, executorAfter.kudosBalance)
        assertEquals(22, targetAfter.kudosBalance)
    }

    @Test
    fun `HOUSE_BONUS should delete card after use`() {
        val card = saveCard(CardType.HOUSE_BONUS)
        usecase.execute(card.id!!, updatedExecutor.id!!)
        assertNull(cardRepository.findById(card.id))
    }

    // --- STEAL_KUDOS ---

    @Test
    fun `STEAL_KUDOS should transfer between 1 and 6 kudos from target to executor`() {
        val card = saveCard(CardType.STEAL_KUDOS)
        usecase.execute(card.id!!, updatedExecutor.id!!, targetUserId = updatedTarget.id!!)

        val executorAfter = userRepository.findById(updatedExecutor.id)!!
        val targetAfter = userRepository.findById(updatedTarget.id)!!

        val stolen = executorAfter.kudosBalance - updatedExecutor.kudosBalance
        assertTrue(stolen in 1..6, "Stolen amount should be between 1 and 6, but was $stolen")
        assertEquals(updatedTarget.kudosBalance - stolen, targetAfter.kudosBalance)
        assertEquals(
            updatedExecutor.kudosBalance + updatedTarget.kudosBalance,
            executorAfter.kudosBalance + targetAfter.kudosBalance
        )
    }

    @Test
    fun `STEAL_KUDOS should throw when no target selected`() {
        val card = saveCard(CardType.STEAL_KUDOS)
        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(card.id!!, updatedExecutor.id!!)
        }
    }

    @Test
    fun `STEAL_KUDOS should throw when targeting yourself`() {
        val card = saveCard(CardType.STEAL_KUDOS)
        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(card.id!!, updatedExecutor.id!!, targetUserId = updatedExecutor.id)
        }
    }

    // --- FORCE_TASK ---

    @Test
    fun `FORCE_TASK should assign open task to target with isForced true`() {
        val task = TestDataFactory.createTask(taskRepository, house.id!!)
        val card = saveCard(CardType.FORCE_TASK)
        usecase.execute(card.id!!, updatedExecutor.id!!, targetUserId = updatedTarget.id!!, targetTaskId = task.id!!)

        val taskAfter = taskRepository.findById(task.id)!!
        assertEquals(updatedTarget.id, taskAfter.assignedTo)
        assertEquals(TaskStatus.ASSIGNED, taskAfter.status)
        assertTrue(taskAfter.isForced)
    }

    @Test
    fun `FORCE_TASK should throw when task is not open`() {
        val task = taskRepository.save(TestDataFactory.createTask(taskRepository, house.id!!).copy(status = TaskStatus.ASSIGNED))
        val card = saveCard(CardType.FORCE_TASK)
        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(card.id!!, updatedExecutor.id!!, targetUserId = updatedTarget.id!!, targetTaskId = task.id!!)
        }
    }

    // --- SKIP_TASK ---

    @Test
    fun `SKIP_TASK should return assigned task to market without penalty`() {
        val task = taskRepository.save(
            TestDataFactory.createTask(taskRepository, house.id!!).copy(
                assignedTo = updatedExecutor.id,
                status = TaskStatus.ASSIGNED
            )
        )
        val card = saveCard(CardType.SKIP_TASK)
        usecase.execute(card.id!!, updatedExecutor.id!!, targetTaskId = task.id!!)

        val taskAfter = taskRepository.findById(task.id)!!
        assertEquals(TaskStatus.OPEN, taskAfter.status)
        assertNull(taskAfter.assignedTo)
        val executorAfter = userRepository.findById(updatedExecutor.id)!!
        assertEquals(50, executorAfter.kudosBalance)
    }

    // --- CLEAN_SLATE ---

    @Test
    fun `CLEAN_SLATE should return forced task to open`() {
        val forcedTask = taskRepository.save(
            TestDataFactory.createTask(taskRepository, house.id!!).copy(
                assignedTo = updatedExecutor.id,
                status = TaskStatus.ASSIGNED,
                isForced = true
            )
        )
        val card = saveCard(CardType.CLEAN_SLATE)
        usecase.execute(card.id!!, updatedExecutor.id!!)

        val taskAfter = taskRepository.findById(forcedTask.id!!)!!
        assertEquals(TaskStatus.OPEN, taskAfter.status)
        assertNull(taskAfter.assignedTo)
        assertFalse(taskAfter.isForced)
    }

    @Test
    fun `CLEAN_SLATE should throw when no forced tasks exist`() {
        val card = saveCard(CardType.CLEAN_SLATE)
        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(card.id!!, updatedExecutor.id!!)
        }
    }

    // --- MARKET_BOOST ---

    @Test
    fun `MARKET_BOOST should add +1 kudos to all open tasks`() {
        val task1 = TestDataFactory.createTask(taskRepository, house.id!!, effort = Effort.LOW)
        val task2 = TestDataFactory.createTask(taskRepository, house.id, effort = Effort.MEDIUM)
        val card = saveCard(CardType.MARKET_BOOST)
        usecase.execute(card.id!!, updatedExecutor.id!!)

        assertEquals(3, taskRepository.findById(task1.id!!)!!.kudosValue)
        assertEquals(5, taskRepository.findById(task2.id!!)!!.kudosValue)
    }

    // --- UNDERDOG_BOOST ---

    @Test
    fun `UNDERDOG_BOOST should add +3 kudos to lowest open task`() {
        val lowTask = TestDataFactory.createTask(taskRepository, house.id!!, effort = Effort.LOW)
        TestDataFactory.createTask(taskRepository, house.id, effort = Effort.HIGH)
        val card = saveCard(CardType.UNDERDOG_BOOST)
        usecase.execute(card.id!!, updatedExecutor.id!!)

        val lowTaskAfter = taskRepository.findById(lowTask.id!!)!!
        assertEquals(5, lowTaskAfter.kudosValue)
    }

    // --- FAST_TRACK ---

    @Test
    fun `FAST_TRACK should approve all PENDING_REVIEW tasks and give kudos to assignees`() {
        val pastTime = LocalDateTime.now().minusDays(1)
        val task1 = taskRepository.save(
            Task.create("Task 1", null, Effort.LOW, null, null, house.id!!, 1L).copy(
                status = TaskStatus.PENDING_REVIEW,
                assignedTo = updatedTarget.id,
                completedAt = pastTime
            )
        )
        val task2 = taskRepository.save(
            Task.create("Task 2", null, Effort.MEDIUM, null, null, house.id, 1L).copy(
                status = TaskStatus.PENDING_REVIEW,
                assignedTo = updatedExecutor.id,
                completedAt = pastTime
            )
        )
        val targetKudosBefore = userRepository.findById(updatedTarget.id!!)!!.kudosBalance
        val card = saveCard(CardType.FAST_TRACK)
        usecase.execute(card.id!!, updatedExecutor.id!!)

        assertEquals(TaskStatus.APPROVED, taskRepository.findById(task1.id!!)!!.status)
        assertEquals(TaskStatus.APPROVED, taskRepository.findById(task2.id!!)!!.status)
        assertEquals(targetKudosBefore + task1.kudosValue, userRepository.findById(updatedTarget.id)!!.kudosBalance)
    }

    @Test
    fun `FAST_TRACK should not give kudos when task was completed after deadline`() {
        val expiredDeadline = LocalDateTime.now().minusDays(2)
        val task = taskRepository.save(
            Task.create("Task", null, Effort.HIGH, null, expiredDeadline, house.id!!, 1L).copy(
                status = TaskStatus.PENDING_REVIEW,
                assignedTo = updatedTarget.id,
                completedAt = LocalDateTime.now().minusDays(1)
            )
        )
        val kudosBefore = userRepository.findById(updatedTarget.id!!)!!.kudosBalance
        val card = saveCard(CardType.FAST_TRACK)
        usecase.execute(card.id!!, updatedExecutor.id!!)

        assertEquals(TaskStatus.APPROVED, taskRepository.findById(task.id!!)!!.status)
        assertEquals(kudosBefore, userRepository.findById(updatedTarget.id)!!.kudosBalance)
    }

    @Test
    fun `FAST_TRACK should throw when no tasks are pending review`() {
        val card = saveCard(CardType.FAST_TRACK)
        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(card.id!!, updatedExecutor.id!!)
        }
    }

    @Test
    fun `FAST_TRACK should delete card after use`() {
        taskRepository.save(
            Task.create("Task", null, Effort.MEDIUM, null, null, house.id!!, 1L).copy(
                status = TaskStatus.PENDING_REVIEW,
                assignedTo = updatedTarget.id,
                completedAt = LocalDateTime.now().minusDays(1)
            )
        )
        val card = saveCard(CardType.FAST_TRACK)
        usecase.execute(card.id!!, updatedExecutor.id!!)
        assertNull(cardRepository.findById(card.id))
    }

    // --- CATEGORY_BOOST ---

    @Test
    fun `CATEGORY_BOOST should add +1 kudos to all eligible tasks in selected category`() {
        val category = TestDataFactory.createCategory(categoryRepository, house.id!!)
        val task1 = TestDataFactory.createTask(taskRepository, house.id, effort = Effort.LOW, categoryId = category.id!!)
        val task2 = TestDataFactory.createTask(taskRepository, house.id, effort = Effort.MEDIUM, categoryId = category.id)
        val card = saveCard(CardType.CATEGORY_BOOST)
        usecase.execute(card.id!!, updatedExecutor.id!!, targetCategoryId = category.id)

        assertEquals(3, taskRepository.findById(task1.id!!)!!.kudosValue)
        assertEquals(5, taskRepository.findById(task2.id!!)!!.kudosValue)
    }

    @Test
    fun `CATEGORY_BOOST should not affect tasks from other categories`() {
        val category1 = TestDataFactory.createCategory(categoryRepository, house.id!!, "Cat1")
        val category2 = TestDataFactory.createCategory(categoryRepository, house.id, "Cat2")
        val task1 = TestDataFactory.createTask(taskRepository, house.id, effort = Effort.MEDIUM, categoryId = category1.id!!)
        val task2 = TestDataFactory.createTask(taskRepository, house.id, effort = Effort.MEDIUM, categoryId = category2.id!!)
        val card = saveCard(CardType.CATEGORY_BOOST)
        usecase.execute(card.id!!, updatedExecutor.id!!, targetCategoryId = category1.id)

        assertEquals(5, taskRepository.findById(task1.id!!)!!.kudosValue)
        assertEquals(4, taskRepository.findById(task2.id!!)!!.kudosValue)
    }

    @Test
    fun `CATEGORY_BOOST should not affect APPROVED or AUTO_APPROVED tasks`() {
        val category = TestDataFactory.createCategory(categoryRepository, house.id!!)
        val approvedTask = taskRepository.save(
            TestDataFactory.createTask(taskRepository, house.id, categoryId = category.id!!).copy(status = TaskStatus.APPROVED)
        )
        val autoApprovedTask = taskRepository.save(
            TestDataFactory.createTask(taskRepository, house.id, categoryId = category.id).copy(status = TaskStatus.AUTO_APPROVED)
        )
        val card = saveCard(CardType.CATEGORY_BOOST)
        usecase.execute(card.id!!, updatedExecutor.id!!, targetCategoryId = category.id)

        assertEquals(4, taskRepository.findById(approvedTask.id!!)!!.kudosValue)
        assertEquals(4, taskRepository.findById(autoApprovedTask.id!!)!!.kudosValue)
    }

    @Test
    fun `CATEGORY_BOOST should throw when no category selected`() {
        val card = saveCard(CardType.CATEGORY_BOOST)
        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(card.id!!, updatedExecutor.id!!)
        }
    }

    // --- VALUE_INFLATION ---

    @Test
    fun `VALUE_INFLATION should double kudos value of selected task`() {
        val task = TestDataFactory.createTask(taskRepository, house.id!!, effort = Effort.MEDIUM)
        val card = saveCard(CardType.VALUE_INFLATION)
        usecase.execute(card.id!!, updatedExecutor.id!!, targetTaskId = task.id!!)

        val taskAfter = taskRepository.findById(task.id)!!
        assertEquals(8, taskAfter.kudosValue)
    }

    // --- GENERAL ---

    @Test
    fun `should throw when card does not exist`() {
        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(999L, updatedExecutor.id!!)
        }
    }

    @Test
    fun `should throw when card does not belong to user`() {
        val card = cardRepository.save(Card.create(updatedTarget.id!!, CardType.HOUSE_BONUS))
        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(card.id!!, updatedExecutor.id!!)
        }
    }

    @Test
    fun `should throw when user does not belong to a house`() {
        val homeless = userRepository.save(User.create("Homeless", "homeless@email.com", "hash"))
        val card = cardRepository.save(Card.create(homeless.id!!, CardType.HOUSE_BONUS))
        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(card.id!!, homeless.id)
        }
    }
}
