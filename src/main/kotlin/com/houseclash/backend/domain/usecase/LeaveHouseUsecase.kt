package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.TaskStatus
import com.houseclash.backend.domain.model.User
import com.houseclash.backend.domain.port.CardRepository
import com.houseclash.backend.domain.port.CategoryRepository
import com.houseclash.backend.domain.port.HouseRepository
import com.houseclash.backend.domain.port.TaskRepository
import com.houseclash.backend.domain.port.UserRepository

class LeaveHouseUsecase(
    private val userRepository: UserRepository,
    private val taskRepository: TaskRepository,
    private val cardRepository: CardRepository,
    private val houseRepository: HouseRepository,
    private val categoryRepository: CategoryRepository
) {
    fun execute(userId: Long): User {
        val user = userRepository.findById(userId)
        requireNotNull(user) { "User does not exist." }

        val houseId = user.houseId
        requireNotNull(houseId) { "User doesn't belong to a house" }

        val houseMembers = userRepository.findByHouseId(houseId)
        val isLastMember = houseMembers.size == 1 && houseMembers.first().id == userId

        if (isLastMember) {
            taskRepository.deleteByHouseId(houseId)
            categoryRepository.deleteByHouseId(houseId)
            houseRepository.delete(houseRepository.findById(houseId)!!)
        } else {
            val userTasks = taskRepository.findByAssignedTo(userId)
            userTasks.forEach { task ->
                if (task.status == TaskStatus.ASSIGNED) {
                    val freedTask = task.copy(
                        status = TaskStatus.OPEN,
                        assignedTo = null,
                        isForced = false,
                    )
                    taskRepository.save(freedTask)
                }
            }
        }

        cardRepository.deleteByUserId(userId)

        return userRepository.save(user.leaveHouse(houseId))
    }
}
