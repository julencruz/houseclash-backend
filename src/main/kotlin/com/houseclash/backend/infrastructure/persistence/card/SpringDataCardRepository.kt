package com.houseclash.backend.infrastructure.persistence.card

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SpringDataCardRepository : JpaRepository<CardJpaEntity, Long> {
    fun findByUserId(userId: Long): List<CardJpaEntity>
    fun deleteByUserId(userId: Long)
}
