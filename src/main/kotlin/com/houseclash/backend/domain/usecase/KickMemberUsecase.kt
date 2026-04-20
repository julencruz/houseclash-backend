package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.ActivityLog
import com.houseclash.backend.domain.model.ActivityLogType
import com.houseclash.backend.domain.model.TaskStatus
import com.houseclash.backend.domain.model.User
import com.houseclash.backend.domain.port.ActivityLogRepository
import com.houseclash.backend.domain.port.CardRepository
import com.houseclash.backend.domain.port.HouseRepository
import com.houseclash.backend.domain.port.TaskRepository
import com.houseclash.backend.domain.port.UserRepository

class KickMemberUsecase(
    private val userRepository: UserRepository,
    private val houseRepository: HouseRepository,
    private val taskRepository: TaskRepository,
    private val cardRepository: CardRepository,
    private val activityLogRepository: ActivityLogRepository,
) {
    fun execute(captainId: Long, kickedUserId: Long): User {
        require(captainId != kickedUserId) { "You cannot kick yourself. Use LeaveHouse or TransferOwnership instead." }

        val captain = userRepository.findById(captainId)
        require(captain != null) { "Captain not found." }
        require(captain.houseId != null) { "Captain does not belong to a house." }
        val kickedUser = userRepository.findById(kickedUserId)
        require(kickedUser != null) { "User to kick not found." }

        require(kickedUser.houseId == captain.houseId) { "User does not belong to your house." }

        val house = houseRepository.findById(captain.houseId)
        require(house != null) { "House not found." }
        require(house.createdBy == captainId) { "Only the house captain can kick users." }

        val userTasks = taskRepository.findByAssignedTo(kickedUserId)
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

        cardRepository.deleteByUserId(kickedUserId)

        val result = userRepository.save(kickedUser.leaveHouse(house.id!!))

        activityLogRepository.save(ActivityLog(
            houseId = house.id,
            type = ActivityLogType.MEMBER_KICKED,
            actorUserId = captainId,
            actorUsername = captain.username,
            targetUserId = kickedUserId,
            targetUsername = kickedUser.username
        ))

        return result
    }
}
