package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.ActivityLog
import com.houseclash.backend.domain.model.ActivityLogType
import com.houseclash.backend.domain.model.House
import com.houseclash.backend.domain.port.ActivityLogRepository
import com.houseclash.backend.domain.port.HouseRepository
import com.houseclash.backend.domain.port.UserRepository

class TransferHouseOwnershipUsecase (
    private val houseRepository: HouseRepository,
    private val userRepository: UserRepository,
    private val activityLogRepository: ActivityLogRepository,
) {
    fun execute(currentOwnerId: Long, newOwnerId: Long): House {
        val currentOwner = userRepository.findById(currentOwnerId)
        requireNotNull(currentOwner) { "Current owner does not exist." }

        val houseId = currentOwner.houseId
        requireNotNull(houseId) { "Current owner does not belong to a house." }

        val house = houseRepository.findById(houseId)
        requireNotNull(house) { "House does not exist." }
        require(house.createdBy == currentOwnerId) { "Only the current captain can transfer ownership." }

        val newOwner = userRepository.findById(newOwnerId)
        requireNotNull(newOwner) { "New owner does not exist." }
        require(newOwner.houseId == houseId) { "The new captain must belong to the same house." }

        val updatedHouse = houseRepository.save(house.transferOwnership(newOwnerId))

        activityLogRepository.save(ActivityLog(
            houseId = houseId,
            type = ActivityLogType.CAPTAIN_TRANSFERRED,
            actorUserId = currentOwnerId,
            actorUsername = currentOwner.username,
            targetUserId = newOwnerId,
            targetUsername = newOwner.username
        ))

        return updatedHouse
    }
}
