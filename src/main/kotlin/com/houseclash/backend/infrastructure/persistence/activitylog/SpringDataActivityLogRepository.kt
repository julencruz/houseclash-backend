package com.houseclash.backend.infrastructure.persistence.activitylog

import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface SpringDataActivityLogRepository : JpaRepository<ActivityLogJpaEntity, Long> {

    fun findByHouseIdOrderByCreatedAtAsc(houseId: Long): List<ActivityLogJpaEntity>

    @Modifying
    @Transactional
    @Query("DELETE FROM ActivityLogJpaEntity a WHERE a.houseId = :houseId AND (a.taskId IS NULL OR a.taskId NOT IN :pendingTaskIds)")
    fun deleteByHouseIdExcludingPendingTasks(
        @Param("houseId") houseId: Long,
        @Param("pendingTaskIds") pendingTaskIds: List<Long>
    )

    @Modifying
    @Transactional
    fun deleteByHouseId(houseId: Long)
}
