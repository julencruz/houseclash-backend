package com.houseclash.backend.infrastructure.persistence.user

import com.houseclash.backend.domain.model.User
import com.houseclash.backend.domain.port.UserRepository
import org.springframework.stereotype.Component

@Component
class UserRepositoryAdapter(
    private val jpaRepository: SpringDataUserRepository
) : UserRepository {

    override fun save(user: User): User {
        val entity = user.toEntity()
        val savedEntity = jpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    override fun findById(id: Long): User? {
        return jpaRepository.findById(id).map { it.toDomain() }.orElse(null)
    }

    override fun findByEmail(email: String): User? {
        return jpaRepository.findByEmail(email)?.toDomain()
    }

    override fun delete(id: Long) {
        jpaRepository.deleteById(id)
    }

    override fun findByHouseId(houseId: Long): List<User> {
        return jpaRepository.findByHouseId(houseId).map { it.toDomain() }
    }
}
