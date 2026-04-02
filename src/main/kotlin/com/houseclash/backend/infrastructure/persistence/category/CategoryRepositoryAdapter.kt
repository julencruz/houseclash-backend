package com.houseclash.backend.infrastructure.persistence.category

import com.houseclash.backend.domain.model.Category
import com.houseclash.backend.domain.port.CategoryRepository
import org.springframework.stereotype.Component

@Component
class CategoryRepositoryAdapter(
    private val jpaRepository: SpringDataCategoryRepository
) : CategoryRepository {

    override fun save(category: Category): Category {
        val entity = category.toEntity()
        return jpaRepository.save(entity).toDomain()
    }

    override fun findById(id: Long): Category? {
        return jpaRepository.findById(id).map { it.toDomain() }.orElse(null)
    }

    override fun findByHouseId(houseId: Long): List<Category> {
        return jpaRepository.findByHouseId(houseId).map { it.toDomain() }
    }

    override fun delete(id: Long) {
        jpaRepository.deleteById(id)
    }

    override fun deleteByHouseId(houseId: Long) {
        jpaRepository.deleteByHouseId(houseId)
    }
}
