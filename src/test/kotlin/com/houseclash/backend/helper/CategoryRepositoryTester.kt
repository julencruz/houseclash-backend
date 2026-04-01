package com.houseclash.backend.helper

import com.houseclash.backend.domain.model.Category
import com.houseclash.backend.domain.port.CategoryRepository

class CategoryRepositoryTester : CategoryRepository {
    private val categories = mutableListOf<Category>()
    private var idCounter = 1L

    override fun save(category: Category): Category {
        val saved = if (category.id == null) {
            category.copy(id = idCounter++)
        } else {
            category
        }
        categories.removeIf { it.id == saved.id }
        categories.add(saved)
        return saved
    }

    override fun findById(id: Long): Category? {
        return categories.find { it.id == id }
    }

    override fun findByHouseId(houseId: Long): List<Category> {
        return categories.filter { it.houseId == houseId }
    }

    override fun deleteByHouseId(houseId: Long) {
        val keysToRemove = categories.filter { it.houseId == houseId }
        keysToRemove.forEach { categories.remove(it) }
    }

    override fun delete(id: Long) {
        categories.removeIf { it.id == id }
    }
}
