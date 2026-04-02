package com.houseclash.backend.infrastructure.persistence.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SpringDataUserRepository : JpaRepository<UserJpaEntity, Long> {
    fun findByEmail(email: String): UserJpaEntity?
    fun findByHouseId(houseId: Long): List<UserJpaEntity>
}
