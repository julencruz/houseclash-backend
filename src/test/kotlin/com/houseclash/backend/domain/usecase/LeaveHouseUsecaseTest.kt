package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.TaskStatus
import com.houseclash.backend.helper.CardRepositoryTester
import com.houseclash.backend.helper.CategoryRepositoryTester
import com.houseclash.backend.helper.HouseRepositoryTester
import com.houseclash.backend.helper.TaskRepositoryTester
import com.houseclash.backend.helper.TestDataFactory
import com.houseclash.backend.helper.UserRepositoryTester
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LeaveHouseUsecaseTest {
    private val userRepository = UserRepositoryTester()
    private val taskRepository = TaskRepositoryTester()
    private val cardRepository = CardRepositoryTester()
    private val houseRepository = HouseRepositoryTester()
    private val categoryRepository = CategoryRepositoryTester()

    private val usecase = LeaveHouseUsecase(userRepository, taskRepository, cardRepository, houseRepository, categoryRepository)

    @Test
    fun `should throw when user does not exist`() {
        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(999L)
        }
    }

    @Test
    fun `should throw when user does not belong to any house`() {
        val userWithoutHouse = TestDataFactory.createUser(userRepository)
        require(userWithoutHouse.id != null)

        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(userWithoutHouse.id)
        }
    }

    @Test
    fun `should completely remove user, reset economy, free tasks and burn cards when OTHER members remain`() {
        val creator = TestDataFactory.createUser(userRepository)
        val house = TestDataFactory.createHouse(houseRepository, userRepository, creator)
        require(house.id != null)

        val leavingUser = TestDataFactory.createUser(userRepository, kudosBalance = 50)
        val userInHouse = userRepository.save(leavingUser.joinHouse(house.id))
        require(userInHouse.id != null)

        val task = TestDataFactory.createTask(taskRepository, house.id)
        val assignedTask = taskRepository.save(task.forceAssignTo(userInHouse.id))
        require(assignedTask.id != null)
        TestDataFactory.createCard(cardRepository, userInHouse.id)

        val result = usecase.execute(userInHouse.id)

        val finalUser = userRepository.findById(userInHouse.id)
        require(finalUser != null)
        val freedTask = taskRepository.findById(assignedTask.id)
        require(freedTask != null)
        val userCards = cardRepository.findByUserId(userInHouse.id)

        assertNull(result.houseId)
        assertEquals(0, finalUser.kudosBalance)
        assertEquals(TaskStatus.OPEN, freedTask.status)
        assertNull(freedTask.assignedTo)
        assertTrue(userCards.isEmpty())
    }

    @Test
    fun `should destroy house, categories and tasks if user is the last member leaving`() {
        val user = TestDataFactory.createUser(userRepository, kudosBalance = 50)
        val house = TestDataFactory.createHouse(houseRepository, userRepository, user)
        require(house.id != null)
        require(user.id != null)
        val houseId = house.id

        TestDataFactory.createCategory(categoryRepository, houseId, "Bany")
        TestDataFactory.createTask(taskRepository, houseId)
        TestDataFactory.createCard(cardRepository, user.id)

        val result = usecase.execute(user.id)

        val finalUser = userRepository.findById(user.id)
        require(finalUser != null)
        assertNull(result.houseId)
        assertNull(finalUser.houseId)
        assertEquals(0, finalUser.kudosBalance)

        assertNull(houseRepository.findById(houseId))
        assertTrue(categoryRepository.findByHouseId(houseId).isEmpty())
        assertTrue(taskRepository.findByHouseIdAndStatus(houseId, TaskStatus.OPEN).isEmpty())
        assertTrue(cardRepository.findByUserId(user.id).isEmpty())
    }
}
