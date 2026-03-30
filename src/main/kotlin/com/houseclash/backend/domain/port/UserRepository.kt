package com.houseclash.backend.domain.port

import com.houseclash.backend.domain.model.User

interface UserRepository {

    fun save(user : User): User
    fun findByHouseId(houseId: Long) : List<User>
    fun findById(id: Long): User?
    fun findByEmail(email: String) : User?
    fun delete(id: Long)

}