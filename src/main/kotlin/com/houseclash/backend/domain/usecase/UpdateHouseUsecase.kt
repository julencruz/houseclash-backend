package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.House
import com.houseclash.backend.domain.port.HouseRepository
import com.houseclash.backend.domain.port.UserRepository

class UpdateHouseUsecase(
    private val houseRepository: HouseRepository,
    private val userRepository: UserRepository
) {
    fun execute(userId: Long, houseId: Long, newName: String): House {
        val user = userRepository.findById(userId)
        require(user != null) {"User not found"}
        val house = houseRepository.findById(houseId)
        require(house != null) { "House not found" }
        require(user.houseId == houseId) { "User does not belong to this house" }

        val updated = house.update(newName)
        return houseRepository.save(updated)
    }
}
