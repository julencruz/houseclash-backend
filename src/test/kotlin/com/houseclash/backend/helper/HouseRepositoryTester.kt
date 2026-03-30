package com.houseclash.backend.helper

import com.houseclash.backend.domain.model.House
import com.houseclash.backend.domain.port.HouseRepository

class HouseRepositoryTester : HouseRepository{

    private val houses = mutableListOf<House>()
    private var idCounter = 1L

    override fun save(house: House): House {
        val savedHouse = if (house.id == null) {
            house.copy(id = idCounter++)
        } else {
            house
        }
        houses.removeIf { it.id == savedHouse.id }
        houses.add(savedHouse)
        return savedHouse
    }

    override fun findById(id: Long): House? {
        return houses.find { it.id == id }
    }

    override fun findByInviteCode(inviteCode: String): House? {
        return houses.find { it.inviteCode == inviteCode }
    }

    override fun delete(house: House) {
        houses.removeIf { it.id == house.id }
    }
}
