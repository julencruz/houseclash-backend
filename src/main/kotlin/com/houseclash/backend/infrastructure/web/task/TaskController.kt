package com.houseclash.backend.infrastructure.web.task

import com.houseclash.backend.domain.model.Recurrence
import com.houseclash.backend.domain.usecase.*
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tasks")
class TaskController(
    private val getActiveTasksUsecase: GetActiveTasksUsecase,
    private val createTaskUsecase: CreateTaskUsecase,
    private val updateTaskUsecase: UpdateTaskUsecase,
    private val deleteTaskUsecase: DeleteTaskUsecase,
    private val assignTaskUsecase: AssignTaskUsecase,
    private val unassignTaskUsecase: UnassignTaskUsecase,
    private val completeTaskUsecase: CompleteTaskUsecase,
    private val validateTaskUsecase: ValidateTaskUsecase
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun getActiveTasks(authentication: Authentication): ResponseEntity<List<TaskResponse>> {
        val userId = authentication.principal as Long
        logger.info("User {} fetching active tasks", userId)
        val tasks = getActiveTasksUsecase.execute(userId)
        logger.info("Returning {} active tasks for user {}", tasks.size, userId)
        return ResponseEntity.ok(tasks.map { it.toResponse() })
    }

    @PostMapping
    fun create(
        @RequestBody request: CreateTaskRequest,
        authentication: Authentication
    ): ResponseEntity<TaskResponse> {
        val userId = authentication.principal as Long
        logger.info("User {} creating task '{}' in house {}", userId, request.title, request.houseId)
        val task = createTaskUsecase.execute(
            userId = userId,
            title = request.title,
            description = request.description,
            effort = request.effort,
            recurrence = request.recurrence?.let { Recurrence.valueOf(it.uppercase()) },
            houseId = request.houseId,
            categoryId = request.categoryId
        )
        logger.info("Task {} created by user {}", task.id, userId)
        return ResponseEntity.status(HttpStatus.CREATED).body(task.toResponse())
    }

    @PatchMapping("/{taskId}")
    fun update(
        @PathVariable taskId: Long,
        @RequestBody request: UpdateTaskRequest,
        authentication: Authentication
    ): ResponseEntity<TaskResponse> {
        val userId = authentication.principal as Long
        logger.info("User {} updating task {}", userId, taskId)
        val task = updateTaskUsecase.execute(
            userId = userId,
            taskId = taskId,
            title = request.title,
            description = request.description,
            effort = request.effort,
            recurrence = request.recurrence,
            categoryId = request.categoryId
        )
        logger.info("Task {} updated by user {}", taskId, userId)
        return ResponseEntity.ok(task.toResponse())
    }

    @DeleteMapping("/{taskId}")
    fun delete(
        @PathVariable taskId: Long,
        authentication: Authentication
    ): ResponseEntity<Void> {
        val userId = authentication.principal as Long
        logger.info("User {} deleting task {}", userId, taskId)
        deleteTaskUsecase.execute(userId, taskId)
        logger.info("Task {} deleted by user {}", taskId, userId)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{taskId}/assign")
    fun assign(
        @PathVariable taskId: Long,
        authentication: Authentication
    ): ResponseEntity<TaskResponse> {
        val userId = authentication.principal as Long
        logger.info("User {} assigning task {}", userId, taskId)
        val task = assignTaskUsecase.execute(taskId, userId)
        logger.info("Task {} assigned to user {}", taskId, userId)
        return ResponseEntity.ok(task.toResponse())
    }

    @PostMapping("/{taskId}/unassign")
    fun unassign(
        @PathVariable taskId: Long,
        authentication: Authentication
    ): ResponseEntity<TaskResponse> {
        val userId = authentication.principal as Long
        logger.info("User {} unassigning from task {}", userId, taskId)
        val task = unassignTaskUsecase.execute(userId, taskId)
        logger.info("Task {} unassigned by user {}", taskId, userId)
        return ResponseEntity.ok(task.toResponse())
    }

    @PostMapping("/{taskId}/complete")
    fun complete(
        @PathVariable taskId: Long
    ): ResponseEntity<TaskResponse> {
        logger.info("Completing task {}", taskId)
        val task = completeTaskUsecase.execute(taskId)
        logger.info("Task {} marked as completed", taskId)
        return ResponseEntity.ok(task.toResponse())
    }

    @PostMapping("/{taskId}/validate")
    fun validate(
        @PathVariable taskId: Long,
        @RequestBody request: ValidateTaskRequest,
        authentication: Authentication
    ): ResponseEntity<TaskResponse> {
        val userId = authentication.principal as Long
        logger.info("User {} validating task {} with decision '{}'", userId, taskId, request.decision)
        val decision = ValidationDecision.valueOf(request.decision.uppercase())
        val task = validateTaskUsecase.execute(taskId, userId, decision)
        logger.info("Task {} validated by user {}", taskId, userId)
        return ResponseEntity.ok(task.toResponse())
    }
}
