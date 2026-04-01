package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.Category
import com.houseclash.backend.domain.port.CategoryRepository
import com.houseclash.backend.domain.port.HouseRepository
import com.houseclash.backend.domain.port.UserRepository

class CreateCategoryHouseUsecase(
    private val houseRepository: HouseRepository,
    private val categoryRepository: CategoryRepository,
    private val userRepository: UserRepository
) {
    fun execute(userId: Long, houseId: Long, name: String, description: String? = null): Category {
        requireNotNull(houseRepository.findById(houseId)) { "House not found for id: $houseId" }

        val user = userRepository.findById(userId)
        requireNotNull(user) { "User not found" }
        require(user.houseId == houseId) { "User does not belong to this house" }

        val existingCategories = categoryRepository.findByHouseId(houseId)
        require(existingCategories.none { it.name.equals(name, ignoreCase = true) })
        { "Category '$name' already exists in this house" }

        val category = Category.create(houseId, name, description)
        return categoryRepository.save(category)
    }
}
