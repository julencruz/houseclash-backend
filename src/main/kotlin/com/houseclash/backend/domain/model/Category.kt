package com.houseclash.backend.domain.model

data class Category(
    val id: Long? = null,
    val houseId: Long,
    val name: String,
    val description: String? = null,
) {
    companion object {
        fun create(houseId: Long, name: String, description: String? = null): Category {
            require(name.isNotBlank()) { "Category name cannot be blank" }
            return Category(houseId = houseId, name = name, description = description)
        }
    }

    fun update(newName: String? = null, newDescription: String? = null): Category {
        newName?.let { require(it.isNotBlank()) { "Category name cannot be blank" } }

        return this.copy(
            name = newName ?: this.name,
            description = newDescription ?: this.description
        )
    }
}
