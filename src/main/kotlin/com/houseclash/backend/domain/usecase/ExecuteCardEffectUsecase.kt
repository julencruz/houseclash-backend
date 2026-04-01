package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.cardlogic.CardEffectContext
import com.houseclash.backend.domain.model.cardlogic.CardEffectResult
import com.houseclash.backend.domain.port.CardRepository
import com.houseclash.backend.domain.port.TaskRepository
import com.houseclash.backend.domain.port.UserRepository


class ExecuteCardEffectUsecase(
    private val cardRepository: CardRepository,
    private val userRepository: UserRepository,
    private val taskRepository: TaskRepository,
) {
    fun execute(
        cardId: Long,
        executingUserId: Long,
        targetUserId: Long? = null,
        targetTaskId: Long? = null,
        targetCategoryId: Long? = null,
    ): CardEffectResult {
        val card = cardRepository.findById(cardId)
        require(card != null) { "Card does not exist" }
        require(card.userId == executingUserId) { "Card does not belong to this user" }

        val executingUser = userRepository.findById(executingUserId)
        require(executingUser != null) { "User does not exist" }
        require(executingUser.houseId != null) { "User must belong to a house to use a card" }

        val houseMembers = userRepository.findByHouseId(executingUser.houseId)
        val houseTasks = taskRepository.findByHouseId(executingUser.houseId)
        val targetUser = targetUserId?.let { userRepository.findById(it) }
        val targetTask = targetTaskId?.let { taskRepository.findById(it) }

        val context = CardEffectContext(
            executingUser = executingUser,
            houseId = executingUser.houseId,
            targetUser = targetUser,
            targetTask = targetTask,
            targetCategoryId = targetCategoryId,
            houseMembers = houseMembers,
            houseTasks = houseTasks,
        )

        val result = card.type.effect.execute(context)

        result.updatedUsers.forEach { userRepository.save(it) }
        result.updatedTasks.forEach { taskRepository.save(it) }
        cardRepository.delete(cardId)

        return result
    }
}
