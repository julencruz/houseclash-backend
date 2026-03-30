package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.House
import com.houseclash.backend.domain.port.HouseRepository
import com.houseclash.backend.domain.port.UserRepository

class CreateHouseUsecase (
    private val userRepository : UserRepository,
    private val houseRepository: HouseRepository,
) {
    fun execute(userId: Long, name: String, description: String = "") : House{
        val user = userRepository.findById(userId)
        require(user != null) { "User does not exist." }
        require(user.houseId == null) { "User is already a member of a house" }

        var newHouse: House
        do {
            newHouse = House.create(userId, name, description)
        } while (houseRepository.findByInviteCode(newHouse.inviteCode) != null)
        val savedHouse = houseRepository.save(newHouse)
        userRepository.save(user.joinHouse(savedHouse.id!!))
        return savedHouse
    }
}