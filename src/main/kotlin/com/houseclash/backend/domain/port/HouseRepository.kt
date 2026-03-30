package com.houseclash.backend.domain.port

import com.houseclash.backend.domain.model.House

interface HouseRepository {

    fun save(house: House) : House
    fun findById(id: Long): House?
    fun findByInviteCode(inviteCode: String) : House?
    fun delete(house: House)
}
