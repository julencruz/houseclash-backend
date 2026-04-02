package com.houseclash.backend.infrastructure.persistence.category

import com.houseclash.backend.domain.model.Category
import jakarta.persistence.*

@Entity
@Table(
    name = "categories",
    uniqueConstraints = [UniqueConstraint(columnNames = ["house_id", "name"])]
)
class CategoryJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val name: String = "",

    @Column(name = "house_id", nullable = false)
    val houseId: Long = 0L
)

fun CategoryJpaEntity.toDomain(): Category {
    return Category(
        id = this.id,
        name = this.name,
        houseId = this.houseId
    )
}

fun Category.toEntity(): CategoryJpaEntity {
    return CategoryJpaEntity(
        id = this.id,
        name = this.name,
        houseId = this.houseId
    )
}
