package com.houseclash.backend.helper

import com.houseclash.backend.domain.model.User
import com.houseclash.backend.domain.port.UserRepository

class UserRepositoryTester : UserRepository {
    private val users = mutableListOf<User>()
    private var idCounter = 1L

    override fun save(user: User): User {
        val savedUser = if (user.id == null) {
            user.copy(id = idCounter++)
        } else {
            user
        }

        users.removeIf { it.id == savedUser.id }
        users.add(savedUser)

        return savedUser
    }

    override fun findByHouseId(houseId: Long): List<User> {
        return users.filter { it.houseId == houseId }
    }

    override fun findById(id: Long): User? {
        return users.find { it.id == id }
    }

    override fun findByEmail(email: String): User? {
        return users.find { it.email == email }
    }

    override fun delete(id: Long) {
        users.removeIf { it.id == id }
    }
}
