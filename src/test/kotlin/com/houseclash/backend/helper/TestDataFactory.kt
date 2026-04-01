package com.houseclash.backend.helper

import com.houseclash.backend.domain.model.*

object TestDataFactory {

    fun createUser(
        repository: UserRepositoryTester,
        username: String = "TestUser",
        email: String = "test@email.com",
        passwordHash: String = "hashed_Password1",
        kudosBalance: Int = 0
    ): User {
        return repository.save(User.create(username, email, passwordHash).copy(kudosBalance = kudosBalance))
    }

    fun createHouse(
        houseRepository: HouseRepositoryTester,
        userRepository: UserRepositoryTester,
        createdBy: User,
        name: String = "Test House"
    ): House {
        val house = houseRepository.save(House.create(createdBy.id!!, name))
        userRepository.save(createdBy.joinHouse(house.id!!))
        return house
    }

    fun createCategory(
        categoryRepository: CategoryRepositoryTester,
        houseId: Long,
        name: String = "Test Category"
    ): Category {
        return categoryRepository.save(Category.create(houseId, name))
    }

    fun createTask(
        taskRepository: TaskRepositoryTester,
        houseId: Long,
        title: String = "Test Task",
        effort: Effort = Effort.MEDIUM,
        categoryId: Long = 1L
    ): Task {
        return taskRepository.save(Task.create(title, null, effort, null, houseId, categoryId))
    }
}
