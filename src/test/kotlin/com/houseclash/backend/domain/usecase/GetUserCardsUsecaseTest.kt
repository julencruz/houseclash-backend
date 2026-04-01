package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.Card
import com.houseclash.backend.domain.model.CardType
import com.houseclash.backend.helper.CardRepositoryTester
import com.houseclash.backend.helper.TestDataFactory
import com.houseclash.backend.helper.UserRepositoryTester
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class GetUserCardsUsecaseTest {
    private val userRepository = UserRepositoryTester()
    private val cardRepository = CardRepositoryTester()
    private val usecase = GetUserCardsUsecase(userRepository, cardRepository)

    private val user = TestDataFactory.createUser(userRepository)

    @Test
    fun `should return empty list when user has no cards`() {
        val cards = usecase.execute(user.id!!)
        assertTrue(cards.isEmpty())
    }

    @Test
    fun `should return all cards belonging to the user`() {
        cardRepository.save(Card.create(user.id!!, CardType.SKIP_TASK))
        cardRepository.save(Card.create(user.id, CardType.STEAL_KUDOS))

        val cards = usecase.execute(user.id)
        assertEquals(2, cards.size)
        assertTrue(cards.all { it.userId == user.id })
    }

    @Test
    fun `should not return cards belonging to another user`() {
        val otherUser = TestDataFactory.createUser(userRepository, "Other", "other@email.com")
        cardRepository.save(Card.create(otherUser.id!!, CardType.HOUSE_BONUS))
        cardRepository.save(Card.create(user.id!!, CardType.GRACE_PERIOD))

        val cards = usecase.execute(user.id)
        assertEquals(1, cards.size)
        assertEquals(user.id, cards.first().userId)
    }

    @Test
    fun `should throw when user does not exist`() {
        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(999L)
        }
    }
}
