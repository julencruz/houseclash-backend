package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.TaskStatus
import com.houseclash.backend.helper.HouseRepositoryTester
import com.houseclash.backend.helper.TaskRepositoryTester
import com.houseclash.backend.helper.TestDataFactory
import com.houseclash.backend.helper.UserRepositoryTester
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DeleteTaskUsecaseTest {
    private val taskRepository = TaskRepositoryTester()
    private val houseRepository = HouseRepositoryTester()
    private val userRepository = UserRepositoryTester()
    private val usecase = DeleteTaskUsecase(taskRepository, houseRepository)

    @Test
    fun `should successfully delete an open task if user is captain`() {
        val captain = TestDataFactory.createUser(userRepository)
        val house = TestDataFactory.createHouse(houseRepository, userRepository, captain)
        require(house.id != null)
        require(captain.id != null)
        val task = TestDataFactory.createTask(taskRepository, house.id)
        require(task.id != null)

        usecase.execute(userId = captain.id, taskId = task.id)

        assertNull(taskRepository.findById(task.id))
    }

    @Test
    fun `should fail if a regular member tries to delete a task`() {
        val captain = TestDataFactory.createUser(userRepository, username = "Captain", email = "c@m.com")
        val house = TestDataFactory.createHouse(houseRepository, userRepository, captain)
        require(house.id != null)
        val task = TestDataFactory.createTask(taskRepository, house.id)
        require(task.id != null)

        val member = TestDataFactory.createUser(userRepository, username = "Member", email = "m@m.com")
        userRepository.save(member.joinHouse(house.id))
        require(member.id != null)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(userId = member.id, taskId = task.id)
        }
        assertEquals("Only the house admin can delete tasks", exception.message)
    }

    @Test
    fun `should fail if trying to delete an approved task`() {
        val captain = TestDataFactory.createUser(userRepository)
        val house = TestDataFactory.createHouse(houseRepository, userRepository, captain)
        require(house.id != null)
        require(captain.id != null)
        val task = TestDataFactory.createTask(taskRepository, house.id)
        require(task.id != null)

        taskRepository.save(task.copy(status = TaskStatus.APPROVED))

        val exception = assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(userId = captain.id, taskId = task.id)
        }
        assertEquals("Approved tasks cannot be deleted to preserve the kudos history", exception.message)
    }
}
