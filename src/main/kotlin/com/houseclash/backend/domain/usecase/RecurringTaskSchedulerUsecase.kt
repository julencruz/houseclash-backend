package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.port.TaskRepository
import java.time.LocalDateTime

class RecurringTaskSchedulerUsecase(
    private val taskRepository: TaskRepository,
    private val clock: () -> LocalDateTime = { LocalDateTime.now() }
) {
    fun execute() {
        val now = clock()

        taskRepository.findRecurringTasksDue().forEach { task ->
            if (task.isDueForReset(now)) {
                taskRepository.save(task.resetForNextCycle())
            }
        }

        taskRepository.findOverdueAssignedRecurringTasks().forEach { task ->
            if (task.isOverdueUncompletedCycle(now)) {
                taskRepository.save(task.resetForNextCycle())
            }
        }
    }
}
