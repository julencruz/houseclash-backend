package com.houseclash.backend.domain.port

import com.houseclash.backend.domain.model.Category

interface CategoryRepository {
    fun save(category: Category): Category
    fun findById(id: Long): Category?
    fun findByHouseId(houseId: Long): List<Category>
    fun deleteByHouseId(houseId: Long)
    fun delete(id: Long)
}
