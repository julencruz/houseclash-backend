package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.port.TaskRepository
import java.time.LocalDateTime

class RecurringTaskSchedulerUsecase(
    private val taskRepository: TaskRepository,
    private val now: LocalDateTime = LocalDateTime.now()
) {
    fun execute() {
        taskRepository.findRecurringTasksDue().forEach { task ->
            if (task.isDueForReset(now)) {
                taskRepository.save(task.resetForNextCycle())
            }
        }
    }
}
