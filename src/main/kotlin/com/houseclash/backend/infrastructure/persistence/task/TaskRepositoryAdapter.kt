package com.houseclash.backend.infrastructure.persistence.task

import com.houseclash.backend.domain.model.Task
import com.houseclash.backend.domain.model.TaskStatus
import com.houseclash.backend.domain.port.TaskRepository
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.resilience.annotation.Retryable
import org.springframework.stereotype.Component

@Component
class TaskRepositoryAdapter(
    private val jpaRepository: SpringDataTaskRepository
) : TaskRepository {

    @Retryable(
        includes = [OptimisticLockingFailureException::class],
        maxRetries = 3,
        delay = 100,
        multiplier = 2.0
    )
    override fun save(task: Task): Task {
        return jpaRepository.save(task.toEntity()).toDomain()
    }

    override fun findById(id: Long): Task? {
        return jpaRepository.findById(id).map { it.toDomain() }.orElse(null)
    }

    override fun findByHouseId(houseId: Long): List<Task> {
        return jpaRepository.findByHouseId(houseId).map { it.toDomain() }
    }

    override fun findByHouseIdAndStatus(houseId: Long, status: TaskStatus): List<Task> {
        return jpaRepository.findByHouseIdAndStatus(houseId, status).map { it.toDomain() }
    }

    override fun findByAssignedTo(userId: Long): List<Task> {
        return jpaRepository.findByAssignedTo(userId).map { it.toDomain() }
    }

    override fun findRecurringTasksDue(): List<Task> {
        val closedStatuses = listOf(
            TaskStatus.APPROVED,
            TaskStatus.AUTO_APPROVED,
            TaskStatus.DISPUTED
        )

        val candidates = jpaRepository.findRecurringCandidates(closedStatuses).map { it.toDomain() }
        return candidates.filter { it.isDueForReset() }
    }

    override fun findOverdueAssignedRecurringTasks(): List<Task> {
        val inProgressStatuses = listOf(TaskStatus.ASSIGNED, TaskStatus.PENDING_REVIEW)
        val candidates = jpaRepository.findRecurringCandidates(inProgressStatuses).map { it.toDomain() }
        return candidates.filter { it.isOverdueUncompletedCycle() }
    }

    override fun findTasksOverdueByDeadline(): List<Task> {
        val inProgressStatuses = listOf(TaskStatus.OPEN, TaskStatus.ASSIGNED, TaskStatus.PENDING_REVIEW)
        return jpaRepository.findByDeadlineNotNullAndStatusIn(inProgressStatuses)
            .map { it.toDomain() }
            .filter { it.isOverdueByDeadline() }
    }

    override fun findByCategoryId(categoryId: Long): List<Task> {
        return jpaRepository.findByCategoryId(categoryId).map { it.toDomain() }
    }

    override fun deleteByHouseId(houseId: Long) {
        jpaRepository.deleteByHouseId(houseId)
    }

    override fun delete(id: Long) {
        jpaRepository.deleteById(id)
    }
}
