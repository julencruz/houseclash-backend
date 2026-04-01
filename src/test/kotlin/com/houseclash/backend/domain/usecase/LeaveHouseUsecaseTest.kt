package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.TaskStatus
import com.houseclash.backend.helper.CardRepositoryTester
import com.houseclash.backend.helper.TaskRepositoryTester
import com.houseclash.backend.helper.TestDataFactory
import com.houseclash.backend.helper.UserRepositoryTester
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class LeaveHouseUsecaseTest {
    private val userRepository = UserRepositoryTester()
    private val taskRepository = TaskRepositoryTester()
    private val cardRepository = CardRepositoryTester()

    private val usecase = LeaveHouseUsecase(userRepository, taskRepository, cardRepository)

    @Test
    fun `should completely remove user, reset economy, free tasks and burn cards`() {
        val user = TestDataFactory.createUser(userRepository).addKudos(50)
        val userInHouse = userRepository.save(user.joinHouse(99L))

        val task = TestDataFactory.createTask(taskRepository, 99L)
        val assignedTask = taskRepository.save(task.forceAssignTo(userInHouse.id!!))

        val card = TestDataFactory.createCard(cardRepository, userInHouse.id)
        cardRepository.save(card)

        val result = usecase.execute(userInHouse.id)

        val finalUser = userRepository.findById(userInHouse.id)!!
        val freedTask = taskRepository.findById(assignedTask.id!!)!!
        val userCards = cardRepository.findByUserId(userInHouse.id)

        assertNull(result.houseId)
        assertNull(finalUser.houseId)
        assertEquals(0, finalUser.kudosBalance)

        assertEquals(TaskStatus.OPEN, freedTask.status)
        assertNull(freedTask.assignedTo)

        assertTrue(userCards.isEmpty())
    }

    @Test
    fun `should throw when user does not exist`() {
        assertThrows<IllegalArgumentException> {
            usecase.execute(999L)
        }
    }

    @Test
    fun `should throw when user does not belong to any house`() {
        val userWithoutHouse = TestDataFactory.createUser(userRepository)

        assertThrows<IllegalArgumentException> {
            usecase.execute(userWithoutHouse.id!!)
        }
    }
}
