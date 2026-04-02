package com.houseclash.backend.infrastructure.web.category
import com.houseclash.backend.domain.usecase.*
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

    @GetMapping
    fun getCategories(authentication: Authentication): ResponseEntity<List<CategoryResponse>> {
        val userId = authentication.principal as Long
        val categories = getHouseCategoriesUsecase.execute(userId)
        return ResponseEntity.ok(categories.map { it.toResponse() })
    }

    @PostMapping
    fun create(
        @RequestBody request: CreateCategoryRequest,
        authentication: Authentication
    ): ResponseEntity<CategoryResponse> {
        val userId = authentication.principal as Long
        val category = createCategoryHouseUsecase.execute(userId, request.houseId, request.name, request.description)
        return ResponseEntity.status(HttpStatus.CREATED).body(category.toResponse())
    }

    @PatchMapping("/{categoryId}")
    fun update(
        @PathVariable categoryId: Long,
        @RequestBody request: UpdateCategoryRequest,
        authentication: Authentication
    ): ResponseEntity<CategoryResponse> {
        val userId = authentication.principal as Long
        val category = updateCategoryUsecase.execute(userId, categoryId, request.name)
        return ResponseEntity.ok(category.toResponse())
    }

    @DeleteMapping("/{categoryId}")
    fun delete(
        @PathVariable categoryId: Long,
        authentication: Authentication
    ): ResponseEntity<Void> {
        val userId = authentication.principal as Long
        deleteCategoryUsecase.execute(userId, categoryId)
        return ResponseEntity.noContent().build()
    }
}
