package com.houseclash.backend.infrastructure.web.task

import com.houseclash.backend.domain.model.Recurrence
import com.houseclash.backend.domain.usecase.*
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

    @GetMapping
    fun getActiveTasks(authentication: Authentication): ResponseEntity<List<TaskResponse>> {
        val userId = authentication.principal as Long
        val tasks = getActiveTasksUsecase.execute(userId)
        return ResponseEntity.ok(tasks.map { it.toResponse() })
    }

    @PostMapping
    fun create(
        @RequestBody request: CreateTaskRequest,
        authentication: Authentication
    ): ResponseEntity<TaskResponse> {
        val userId = authentication.principal as Long
        val task = createTaskUsecase.execute(
            userId = userId,
            title = request.title,
            description = request.description,
            effort = request.effort,
            recurrence = request.recurrence?.let { Recurrence.valueOf(it.uppercase()) },
            houseId = request.houseId,
            categoryId = request.categoryId
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(task.toResponse())
    }

    @PatchMapping("/{taskId}")
    fun update(
        @PathVariable taskId: Long,
        @RequestBody request: UpdateTaskRequest,
        authentication: Authentication
    ): ResponseEntity<TaskResponse> {
        val userId = authentication.principal as Long
        val task = updateTaskUsecase.execute(
            userId = userId,
            taskId = taskId,
            title = request.title,
            description = request.description,
            effort = request.effort,
            recurrence = request.recurrence,
            categoryId = request.categoryId
        )
        return ResponseEntity.ok(task.toResponse())
    }

    @DeleteMapping("/{taskId}")
    fun delete(
        @PathVariable taskId: Long,
        authentication: Authentication
    ): ResponseEntity<Void> {
        val userId = authentication.principal as Long
        deleteTaskUsecase.execute(userId, taskId)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{taskId}/assign")
    fun assign(
        @PathVariable taskId: Long,
        authentication: Authentication
    ): ResponseEntity<TaskResponse> {
        val userId = authentication.principal as Long
        val task = assignTaskUsecase.execute(taskId, userId)
        return ResponseEntity.ok(task.toResponse())
    }

    @PostMapping("/{taskId}/unassign")
    fun unassign(
        @PathVariable taskId: Long,
        authentication: Authentication
    ): ResponseEntity<TaskResponse> {
        val userId = authentication.principal as Long
        val task = unassignTaskUsecase.execute(userId, taskId)
        return ResponseEntity.ok(task.toResponse())
    }

    @PostMapping("/{taskId}/complete")
    fun complete(
        @PathVariable taskId: Long
    ): ResponseEntity<TaskResponse> {
        val task = completeTaskUsecase.execute(taskId)
        return ResponseEntity.ok(task.toResponse())
    }

    @PostMapping("/{taskId}/validate")
    fun validate(
        @PathVariable taskId: Long,
        @RequestBody request: ValidateTaskRequest,
        authentication: Authentication
    ): ResponseEntity<TaskResponse> {
        val userId = authentication.principal as Long
        val decision = ValidationDecision.valueOf(request.decision.uppercase())
        val task = validateTaskUsecase.execute(taskId, userId, decision)
        return ResponseEntity.ok(task.toResponse())
    }
}
