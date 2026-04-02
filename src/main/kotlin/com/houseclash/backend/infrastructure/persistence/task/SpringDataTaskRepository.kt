package com.houseclash.backend.infrastructure.persistence.task

import com.houseclash.backend.domain.model.TaskStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface SpringDataTaskRepository : JpaRepository<TaskJpaEntity, Long> {

    fun findByHouseId(houseId: Long): List<TaskJpaEntity>
    fun findByHouseIdAndStatus(houseId: Long, status: TaskStatus): List<TaskJpaEntity>
    fun findByAssignedTo(userId: Long): List<TaskJpaEntity>
    fun findByCategoryId(categoryId: Long): List<TaskJpaEntity>
    fun deleteByHouseId(houseId: Long)

    @Query(
        "SELECT t FROM TaskJpaEntity t " +
                "WHERE t.recurrence IS NOT NULL AND t.status IN :statuses"
    )
    fun findRecurringCandidates(@Param("statuses") statuses: List<TaskStatus>): List<TaskJpaEntity>
}
