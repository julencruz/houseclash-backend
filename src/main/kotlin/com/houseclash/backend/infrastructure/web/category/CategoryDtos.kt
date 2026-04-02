package com.houseclash.backend.infrastructure.web.category

import com.houseclash.backend.domain.model.Category

data class CreateCategoryRequest(
    val name: String,
    val description: String? = null,
    val houseId: Long,
)

data class UpdateCategoryRequest(
    val name: String
)

data class CategoryResponse(
    val id: Long,
    val houseId: Long,
    val name: String,
    val description: String?
)

fun Category.toResponse() = CategoryResponse(
    id = this.id!!,
    houseId = this.houseId,
    name = this.name,
    description = this.description
)
