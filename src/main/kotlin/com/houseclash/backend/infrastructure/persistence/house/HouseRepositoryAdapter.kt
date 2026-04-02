package com.houseclash.backend.infrastructure.persistence.house

import com.houseclash.backend.domain.model.House
import com.houseclash.backend.domain.port.HouseRepository
import org.springframework.stereotype.Component

@Component
class HouseRepositoryAdapter(
    private val jpaRepository: SpringDataHouseRepository
) : HouseRepository {

    override fun save(house: House): House {
        val entity = house.toEntity()
        return jpaRepository.save(entity).toDomain()
    }

    override fun findById(id: Long): House? {
        return jpaRepository.findById(id).map { it.toDomain() }.orElse(null)
    }

    override fun findByInviteCode(inviteCode: String): House? {
        return jpaRepository.findByInviteCode(inviteCode)?.toDomain()
    }

    override fun delete(house: House) {
        jpaRepository.deleteById(house.id!!)
    }
}
