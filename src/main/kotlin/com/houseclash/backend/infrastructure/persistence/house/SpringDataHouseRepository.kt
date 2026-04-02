package com.houseclash.backend.infrastructure.persistence.house

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SpringDataHouseRepository : JpaRepository<HouseJpaEntity, Long> {

    fun findByInviteCode(inviteCode: String): HouseJpaEntity?
}
