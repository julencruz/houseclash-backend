package com.houseclash.backend.infrastructure.web

import org.slf4j.LoggerFactory
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException
import java.time.LocalDateTime

data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String
)

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(javaClass)

    // --- 400 Bad Request ---

    /**
     * Handles domain business rule violations thrown by require() or check().
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        logger.warn("Business rule violation: {}", ex.message)
        return ResponseEntity.badRequest().body(
            ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Bad Request",
                message = ex.message ?: "Invalid request"
            )
        )
    }

    /**
     * Handles invalid state transitions in domain logic.
     */
    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(ex: IllegalStateException): ResponseEntity<ErrorResponse> {
        logger.warn("Invalid state: {}", ex.message)
        return ResponseEntity.badRequest().body(
            ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Bad Request",
                message = ex.message ?: "Invalid state"
            )
        )
    }

    /**
     * Handles malformed or unreadable JSON request bodies.
     */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadableBody(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        logger.warn("Malformed request body: {}", ex.message)
        return ResponseEntity.badRequest().body(
            ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Bad Request",
                message = "Malformed or unreadable request body"
            )
        )
    }

    /**
     * Handles Bean Validation failures (@Valid / @Validated annotations).
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val details = ex.bindingResult.fieldErrors.joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
        logger.warn("Validation failed: {}", details)
        return ResponseEntity.badRequest().body(
            ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Validation Failed",
                message = details.ifBlank { "Request validation failed" }
            )
        )
    }

    // --- 401 Unauthorized ---

    /**
     * Handles authentication failures (missing or invalid JWT token).
     */
    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthentication(ex: AuthenticationException): ResponseEntity<ErrorResponse> {
        logger.warn("Authentication failure: {}", ex.message)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            ErrorResponse(
                status = HttpStatus.UNAUTHORIZED.value(),
                error = "Unauthorized",
                message = "Authentication required. Please provide a valid token."
            )
        )
    }

    // --- 403 Forbidden ---

    /**
     * Handles authorization failures (authenticated but not allowed to perform the action).
     */
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(ex: AccessDeniedException): ResponseEntity<ErrorResponse> {
        logger.warn("Access denied: {}", ex.message)
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            ErrorResponse(
                status = HttpStatus.FORBIDDEN.value(),
                error = "Forbidden",
                message = "You do not have permission to perform this action"
            )
        )
    }

    // --- 404 Not Found ---

    /**
     * Handles requests to non-existent endpoints.
     */
    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFound(ex: NoResourceFoundException): ResponseEntity<ErrorResponse> {
        logger.warn("Resource not found: {}", ex.message)
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ErrorResponse(
                status = HttpStatus.NOT_FOUND.value(),
                error = "Not Found",
                message = "The requested resource does not exist"
            )
        )
    }

    // --- 405 Method Not Allowed ---

    /**
     * Handles requests using an unsupported HTTP method on a valid endpoint.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotAllowed(ex: HttpRequestMethodNotSupportedException): ResponseEntity<ErrorResponse> {
        logger.warn("Method not allowed: {}", ex.message)
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(
            ErrorResponse(
                status = HttpStatus.METHOD_NOT_ALLOWED.value(),
                error = "Method Not Allowed",
                message = "HTTP method '${ex.method}' is not supported for this endpoint"
            )
        )
    }

    // --- 409 Conflict ---

    /**
     * Handles optimistic locking conflicts (@Version).
     * Occurs when two processes read the same task and the second one tries
     * to save with a stale version number. After retries are exhausted, this is returned.
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException::class, OptimisticLockingFailureException::class)
    fun handleOptimisticLockingFailure(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.warn("Optimistic locking conflict detected: {}", ex.message)
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            ErrorResponse(
                status = HttpStatus.CONFLICT.value(),
                error = "Conflict",
                message = "The resource was modified by another process. Please try again."
            )
        )
    }

    // --- 500 Internal Server Error ---

    /**
     * Catch-all handler for any unexpected exception not covered above.
     */
    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error: {}", ex.message, ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ErrorResponse(
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                error = "Internal Server Error",
                message = "An unexpected error occurred. Please try again later."
            )
        )
    }
}
