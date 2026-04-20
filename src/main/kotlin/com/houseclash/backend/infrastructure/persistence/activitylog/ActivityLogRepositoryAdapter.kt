package com.houseclash.backend.infrastructure.persistence.activitylog

import com.houseclash.backend.domain.model.ActivityLog
import com.houseclash.backend.domain.port.ActivityLogRepository
import org.springframework.stereotype.Component

@Component
class ActivityLogRepositoryAdapter(
    private val jpaRepository: SpringDataActivityLogRepository
) : ActivityLogRepository {

    override fun save(log: ActivityLog): ActivityLog {
        return jpaRepository.save(log.toEntity()).toDomain()
    }

    override fun findByHouseIdOrderByCreatedAtAsc(houseId: Long): List<ActivityLog> {
        return jpaRepository.findByHouseIdOrderByCreatedAtAsc(houseId).map { it.toDomain() }
    }

    override fun deleteOldLogsExceptPendingReview(houseId: Long, pendingTaskIds: List<Long>) {
        if (pendingTaskIds.isEmpty()) {
            jpaRepository.deleteByHouseId(houseId)
        } else {
            jpaRepository.deleteByHouseIdExcludingPendingTasks(houseId, pendingTaskIds)
        }
    }
}
