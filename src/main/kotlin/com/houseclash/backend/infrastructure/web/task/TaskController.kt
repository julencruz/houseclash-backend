package com.houseclash.backend.infrastructure.web.task

import com.houseclash.backend.domain.model.Recurrence
import com.houseclash.backend.domain.usecase.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasques", description = "Gestió de les tasques de la llar")
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

    @Operation(summary = "Obtenir tasques actives", description = "Retorna totes les tasques actives de la llar a la qual pertany l'usuari autenticat")
    @GetMapping
    fun getActiveTasks(authentication: Authentication): ResponseEntity<List<TaskResponse>> {
        val userId = authentication.principal as Long
        logger.info("User {} fetching active tasks", userId)
        val tasks = getActiveTasksUsecase.execute(userId)
        logger.info("Returning {} active tasks for user {}", tasks.size, userId)
        return ResponseEntity.ok(tasks.map { it.toResponse() })
    }

    @Operation(summary = "Crear tasca", description = "Crea una nova tasca a la llar indicada. Es pot assignar una categoria i definir la recurrència")
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
            deadline = request.deadline,
            houseId = request.houseId,
            categoryId = request.categoryId
        )
        logger.info("Task {} created by user {}", task.id, userId)
        return ResponseEntity.status(HttpStatus.CREATED).body(task.toResponse())
    }

    @Operation(summary = "Actualitzar tasca", description = "Modifica el títol, la descripció, l'esforç, la recurrència o la categoria d'una tasca existent")
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
            deadline = request.deadline,
            categoryId = request.categoryId
        )
        logger.info("Task {} updated by user {}", taskId, userId)
        return ResponseEntity.ok(task.toResponse())
    }

    @Operation(summary = "Eliminar tasca", description = "Elimina permanentment una tasca. Només ho pot fer el creador o un membre amb permisos de la llar")
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

    @Operation(summary = "Assignar-se una tasca", description = "L'usuari autenticat s'assigna a ell mateix la tasca indicada per fer-se'n responsable")
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

    @Operation(summary = "Desassignar-se d'una tasca", description = "L'usuari autenticat es desassigna de la tasca indicada, deixant-la disponible per a altres membres")
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

    @Operation(summary = "Marcar tasca com a completada", description = "L'usuari assignat marca la tasca com a completada. Queda pendent de validació per part d'un altre membre")
    @PostMapping("/{taskId}/complete")
    fun complete(
        @PathVariable taskId: Long,
        authentication: Authentication
    ): ResponseEntity<TaskResponse> {
        val userId = authentication.principal as Long
        logger.info("User {} completing task {}", userId, taskId)
        val task = completeTaskUsecase.execute(taskId, userId)
        logger.info("Task {} marked as completed by user {}", taskId, userId)
        return ResponseEntity.ok(task.toResponse())
    }

    @Operation(summary = "Validar tasca completada", description = "Un membre de la llar aprova o rebutja una tasca marcada com a completada. La decisió pot ser APPROVED o REJECTED")
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
