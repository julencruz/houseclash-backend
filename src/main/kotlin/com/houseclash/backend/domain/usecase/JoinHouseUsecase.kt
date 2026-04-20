package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.ActivityLog
import com.houseclash.backend.domain.model.ActivityLogType
import com.houseclash.backend.domain.model.User
import com.houseclash.backend.domain.port.ActivityLogRepository
import com.houseclash.backend.domain.port.HouseRepository
import com.houseclash.backend.domain.port.UserRepository

class JoinHouseUsecase(
    private val userRepository: UserRepository,
    private val houseRepository: HouseRepository,
    private val activityLogRepository: ActivityLogRepository,
) {
    companion object {
        const val WELCOME_BONUS_KUDOS = 50
    }

    fun execute(userId: Long, inviteCode: String): User {
        val user = userRepository.findById(userId)
        require(user != null) { "User does not exist." }
        require(user.houseId == null) { "User already belongs to a house" }

        val house = houseRepository.findByInviteCode(inviteCode)
        require(house != null) { "House not found for invite code: $inviteCode" }

        val isFirstVisit = !user.hasVisitedHouse(house.id!!)
        var updatedUser = user.joinHouse(house.id)

        if (isFirstVisit) {
            updatedUser = updatedUser.addKudos(WELCOME_BONUS_KUDOS)
        }

        val savedUser = userRepository.save(updatedUser)

        activityLogRepository.save(ActivityLog(
            houseId = house.id,
            type = ActivityLogType.MEMBER_JOINED,
            actorUserId = userId,
            actorUsername = user.username
        ))

        return savedUser
    }
}
