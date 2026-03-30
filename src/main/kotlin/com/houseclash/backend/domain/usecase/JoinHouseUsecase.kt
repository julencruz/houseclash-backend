package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.User
import com.houseclash.backend.domain.port.HouseRepository
import com.houseclash.backend.domain.port.UserRepository

class JoinHouseUsecase (
    private val userRepository : UserRepository,
    private val houseRepository: HouseRepository,
) {
    fun execute(userId: Long, inviteCode: String) : User {
        val user = userRepository.findById(userId)
        require(user != null) { "User does not exist." }
        require(user.houseId == null) { "User already belongs to a house" }

        val house = houseRepository.findByInviteCode(inviteCode)
        require(house != null) { "House not found for invite code: $inviteCode" }

        val updatedUser = user.joinHouse(house.id!!)
        return userRepository.save(updatedUser)
    }
}
