package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.RankingPeriod
import com.houseclash.backend.domain.model.TaskStatus
import com.houseclash.backend.helper.TaskRepositoryTester
import com.houseclash.backend.helper.TestDataFactory
import com.houseclash.backend.helper.UserRepositoryTester
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class GetHouseRankingUsecaseTest {
    private val userRepository = UserRepositoryTester()
    private val taskRepository = TaskRepositoryTester()
    private val usecase = GetHouseRankingUsecase(userRepository, taskRepository)

    @Test
    fun `should rank members primarily by kudos and secondarily by tasks completed`() {
        val houseId = 1L
        val user1 = userRepository.save(
            TestDataFactory.createUser(userRepository, username = "Rey", email = "1@m.com", kudosBalance = 100)
                .joinHouse(houseId)
        )
        val user2 = userRepository.save(
            TestDataFactory.createUser(userRepository, username = "Obrero", email = "2@m.com", kudosBalance = 50)
                .joinHouse(houseId)
        )
        val user3 = userRepository.save(
            TestDataFactory.createUser(userRepository, username = "Vago", email = "3@m.com", kudosBalance = 50)
                .joinHouse(houseId)
        )

        val task1 = TestDataFactory.createTask(taskRepository, houseId)
        taskRepository.save(task1.copy(assignedTo = user2.id, status = TaskStatus.APPROVED, completedAt = LocalDateTime.now()))
        val task2 = TestDataFactory.createTask(taskRepository, houseId)
        taskRepository.save(task2.copy(assignedTo = user2.id, status = TaskStatus.AUTO_APPROVED, completedAt = LocalDateTime.now()))
        val task3 = TestDataFactory.createTask(taskRepository, houseId)
        taskRepository.save(task3.copy(assignedTo = user3.id, status = TaskStatus.APPROVED, completedAt = LocalDateTime.now()))
        val result = usecase.execute(houseId, RankingPeriod.ALL_TIME)
        assertEquals(3, result.size)
        assertEquals(user1.id, result[0].user.id)
        assertEquals(1, result[0].rank)
        assertEquals(user2.id, result[1].user.id)
        assertEquals(2, result[1].rank)
        assertEquals(2, result[1].tasksCompleted)
        assertEquals(user3.id, result[2].user.id)
        assertEquals(3, result[2].rank)
    }

    @Test
    fun `should filter tasks correctly by ranking period`() {
        val houseId = 1L
        val user = userRepository.save(
            TestDataFactory.createUser(userRepository, kudosBalance = 10).joinHouse(houseId)
        )
        val baseTask = TestDataFactory.createTask(taskRepository, houseId)
        taskRepository.save(baseTask.copy(id = 1L, assignedTo = user.id, status = TaskStatus.APPROVED, completedAt = LocalDateTime.now().minusDays(2)))
        taskRepository.save(baseTask.copy(id = 2L, assignedTo = user.id, status = TaskStatus.APPROVED, completedAt = LocalDateTime.now().minusDays(15)))
        taskRepository.save(baseTask.copy(id = 3L, assignedTo = user.id, status = TaskStatus.APPROVED, completedAt = LocalDateTime.now().minusMonths(2)))
        val weeklyRanking = usecase.execute(houseId, RankingPeriod.WEEK)
        assertEquals(1, weeklyRanking.first().tasksCompleted)
        val monthlyRanking = usecase.execute(houseId, RankingPeriod.MONTH)
        assertEquals(2, monthlyRanking.first().tasksCompleted)
        val allTimeRanking = usecase.execute(houseId, RankingPeriod.ALL_TIME)
        assertEquals(3, allTimeRanking.first().tasksCompleted)
    }

    @Test
    fun `should only count tasks with APPROVED or AUTO_APPROVED status`() {
        val houseId = 1L
        val user = userRepository.save(
            TestDataFactory.createUser(userRepository, kudosBalance = 10).joinHouse(houseId)
        )
        val baseTask = TestDataFactory.createTask(taskRepository, houseId)
        val now = LocalDateTime.now()
        taskRepository.save(baseTask.copy(id = 1L, assignedTo = user.id, status = TaskStatus.APPROVED, completedAt = now))
        taskRepository.save(baseTask.copy(id = 2L, assignedTo = user.id, status = TaskStatus.AUTO_APPROVED, completedAt = now))
        taskRepository.save(baseTask.copy(id = 3L, assignedTo = user.id, status = TaskStatus.PENDING_REVIEW, completedAt = now))
        taskRepository.save(baseTask.copy(id = 4L, assignedTo = user.id, status = TaskStatus.DISPUTED, completedAt = now))
        taskRepository.save(baseTask.copy(id = 5L, assignedTo = user.id, status = TaskStatus.ASSIGNED, completedAt = null))
        val result = usecase.execute(houseId, RankingPeriod.ALL_TIME)
        assertEquals(2, result.first().tasksCompleted)
    }

    @Test
    fun `should throw when house has no members`() {
        val exception = assertThrows<IllegalArgumentException> {
            usecase.execute(999L, RankingPeriod.ALL_TIME)
        }
        assertEquals("No members found in this house", exception.message)
    }
}
