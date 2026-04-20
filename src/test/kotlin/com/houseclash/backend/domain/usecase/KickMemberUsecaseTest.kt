package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.TaskStatus
import com.houseclash.backend.helper.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class KickMemberUsecaseTest {
    private val userRepository = UserRepositoryTester()
    private val houseRepository = HouseRepositoryTester()
    private val taskRepository = TaskRepositoryTester()
    private val cardRepository = CardRepositoryTester()
    private val activityLogRepository = ActivityLogRepositoryTester()

    private val usecase = KickMemberUsecase(userRepository, houseRepository, taskRepository, cardRepository, activityLogRepository)

    @Test
    fun `should successfully kick member, free tasks and burn cards`() {
        val captain = TestDataFactory.createUser(userRepository, username = "Captain", email = "c@m.com")
        val house = TestDataFactory.createHouse(houseRepository, userRepository, captain)
        require(house.id != null)
        require(captain.id != null)

        val victim = TestDataFactory.createUser(userRepository, username = "Victim", email = "v@m.com")
        val userInHouse = userRepository.save(victim.joinHouse(house.id))
        require(userInHouse.id != null)

        val task = TestDataFactory.createTask(taskRepository, house.id)
        val assignedTask = taskRepository.save(task.forceAssignTo(userInHouse.id))
        require(assignedTask.id != null)
        TestDataFactory.createCard(cardRepository, userInHouse.id)

        val result = usecase.execute(captainId = captain.id, kickedUserId = userInHouse.id)

        assertNull(result.houseId)

        val freedTask = taskRepository.findById(assignedTask.id)
        require(freedTask != null)
        assertEquals(TaskStatus.OPEN, freedTask.status)
        assertNull(freedTask.assignedTo)

        val victimCards = cardRepository.findByUserId(userInHouse.id)
        assertTrue(victimCards.isEmpty())
    }

    @Test
    fun `should fail if captain tries to kick himself`() {
        val captain = TestDataFactory.createUser(userRepository)
        TestDataFactory.createHouse(houseRepository, userRepository, captain)
        require(captain.id != null)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(captainId = captain.id, kickedUserId = captain.id)
        }
        assertEquals("You cannot kick yourself. Use LeaveHouse or TransferOwnership instead.", exception.message)
    }

    @Test
    fun `should fail if user to kick belongs to another house`() {
        val captain = TestDataFactory.createUser(userRepository, username = "Captain", email = "c@m.com")
        TestDataFactory.createHouse(houseRepository, userRepository, captain)
        require(captain.id != null)

        val otherGuy = TestDataFactory.createUser(userRepository, username = "Other", email = "o@m.com")
        TestDataFactory.createHouse(houseRepository, userRepository, otherGuy)
        require(otherGuy.id != null)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(captainId = captain.id, kickedUserId = otherGuy.id)
        }
        assertEquals("User does not belong to your house.", exception.message)
    }
}
