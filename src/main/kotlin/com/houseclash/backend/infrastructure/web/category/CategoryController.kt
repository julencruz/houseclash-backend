package com.houseclash.backend.infrastructure.web.category
import com.houseclash.backend.domain.usecase.*
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/categories")
class CategoryController(
    private val getHouseCategoriesUsecase: GetHouseCategoriesUsecase,
    private val createCategoryHouseUsecase: CreateCategoryHouseUsecase,
    private val updateCategoryUsecase: UpdateCategoryUsecase,
    private val deleteCategoryUsecase: DeleteCategoryUsecase,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun getCategories(authentication: Authentication): ResponseEntity<List<CategoryResponse>> {
        val userId = authentication.principal as Long
        logger.info("User {} fetching house categories", userId)
        val categories = getHouseCategoriesUsecase.execute(userId)
        logger.info("Returning {} categories for user {}", categories.size, userId)
        return ResponseEntity.ok(categories.map { it.toResponse() })
    }

    @PostMapping
    fun create(
        @RequestBody request: CreateCategoryRequest,
        authentication: Authentication
    ): ResponseEntity<CategoryResponse> {
        val userId = authentication.principal as Long
        logger.info("User {} creating category '{}' in house {}", userId, request.name, request.houseId)
        val category = createCategoryHouseUsecase.execute(userId, request.houseId, request.name, request.description)
        logger.info("Category {} created by user {}", category.id, userId)
        return ResponseEntity.status(HttpStatus.CREATED).body(category.toResponse())
    }

    @PatchMapping("/{categoryId}")
    fun update(
        @PathVariable categoryId: Long,
        @RequestBody request: UpdateCategoryRequest,
        authentication: Authentication
    ): ResponseEntity<CategoryResponse> {
        val userId = authentication.principal as Long
        logger.info("User {} updating category {}", userId, categoryId)
        val category = updateCategoryUsecase.execute(userId, categoryId, request.name)
        logger.info("Category {} updated by user {}", categoryId, userId)
        return ResponseEntity.ok(category.toResponse())
    }

    @DeleteMapping("/{categoryId}")
    fun delete(
        @PathVariable categoryId: Long,
        authentication: Authentication
    ): ResponseEntity<Void> {
        val userId = authentication.principal as Long
        logger.info("User {} deleting category {}", userId, categoryId)
        deleteCategoryUsecase.execute(userId, categoryId)
        logger.info("Category {} deleted by user {}", categoryId, userId)
        return ResponseEntity.noContent().build()
    }
}
