package com.houseclash.backend.infrastructure.persistence.category

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SpringDataCategoryRepository : JpaRepository<CategoryJpaEntity, Long> {

    fun findByHouseId(houseId: Long): List<CategoryJpaEntity>
    fun deleteByHouseId(houseId: Long)
}
