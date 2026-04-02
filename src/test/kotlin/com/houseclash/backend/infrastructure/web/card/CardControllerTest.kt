package com.houseclash.backend.infrastructure.web.card

import com.houseclash.backend.domain.model.CardType
import com.houseclash.backend.domain.model.House
import com.houseclash.backend.domain.model.User
import com.houseclash.backend.domain.usecase.*
import com.houseclash.backend.helper.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

class CardControllerTest {

    private val userRepository = UserRepositoryTester()
    private val houseRepository = HouseRepositoryTester()
    private val cardRepository = CardRepositoryTester()
    private val taskRepository = TaskRepositoryTester()
    private val passwordEncoder = PasswordEncoderTester()

    private val registerUserUsecase = RegisterUserUsecase(userRepository, passwordEncoder)
    private val createHouseUsecase = CreateHouseUsecase(userRepository, houseRepository)
    private val joinHouseUsecase = JoinHouseUsecase(userRepository, houseRepository)
    private val getUserCardsUsecase = GetUserCardsUsecase(userRepository, cardRepository)
    private val openCardPackUsecase = OpenCardPackUsecase(userRepository, cardRepository)
    private val executeCardEffectUsecase = ExecuteCardEffectUsecase(cardRepository, userRepository, taskRepository)

    private val controller = CardController(
        getUserCardsUsecase,
        openCardPackUsecase,
        executeCardEffectUsecase
    )

    private lateinit var owner: User
    private lateinit var house: House

    @BeforeEach
    fun setUp() {
        owner = registerUserUsecase.execute("Owner", "owner@email.com", "Password1")
        house = createHouseUsecase.execute(owner.id!!, "Test House")
        owner = userRepository.findById(owner.id!!)!!
    }

    private fun authAs(user: User) = UsernamePasswordAuthenticationToken(user.id!!, null, emptyList())

    // ---- getMyCards ----

    @Test
    fun `should return 200 with empty list when user has no cards`() {
        val response = controller.getMyCards(authAs(owner))

        assertEquals(HttpStatus.OK, response.statusCode)
        assertTrue(response.body!!.isEmpty())
    }

    @Test
    fun `should return 200 with user cards`() {
        cardRepository.save(com.houseclash.backend.domain.model.Card.create(owner.id!!, CardType.HOUSE_BONUS))
        cardRepository.save(com.houseclash.backend.domain.model.Card.create(owner.id!!, CardType.SKIP_TASK))

        val response = controller.getMyCards(authAs(owner))

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(2, response.body?.size)
    }

    @Test
    fun `should only return cards belonging to the requesting user`() {
        val other = registerUserUsecase.execute("Other", "other@email.com", "Password1")
        joinHouseUsecase.execute(other.id!!, house.inviteCode)
        cardRepository.save(com.houseclash.backend.domain.model.Card.create(other.id, CardType.HOUSE_BONUS))
        cardRepository.save(com.houseclash.backend.domain.model.Card.create(owner.id!!, CardType.SKIP_TASK))

        val response = controller.getMyCards(authAs(owner))

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(1, response.body?.size)
        assertTrue(response.body!!.all { it.userId == owner.id })
    }

    // ---- openPack ----

    @Test
    fun `should return 200 with exactly 4 cards when user has enough kudos`() {
        owner = userRepository.save(owner.addKudos(50))

        val response = controller.openPack(authAs(owner))

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(OpenCardPackUsecase.CARDS_PER_PACK, response.body?.size)
    }

    @Test
    fun `should deduct kudos after opening a pack`() {
        owner = userRepository.save(owner.addKudos(50))

        controller.openPack(authAs(owner))

        val updatedUser = userRepository.findById(owner.id!!)!!
        assertEquals(0, updatedUser.kudosBalance)
    }

    @Test
    fun `should throw when user has insufficient kudos to open a pack`() {
        assertThrows(IllegalArgumentException::class.java) {
            controller.openPack(authAs(owner))
        }
    }

    @Test
    fun `should throw when user does not belong to a house`() {
        val loner = registerUserUsecase.execute("Loner", "loner@email.com", "Password1")
        loner.let { userRepository.save(it.copy(kudosBalance = 100)) }

        assertThrows(IllegalArgumentException::class.java) {
            controller.openPack(authAs(loner))
        }
    }

    // ---- useCard ----

    @Test
    fun `should return 200 with effect result when using HOUSE_BONUS card`() {
        val card = cardRepository.save(com.houseclash.backend.domain.model.Card.create(owner.id!!, CardType.HOUSE_BONUS))

        val response = controller.useCard(card.id!!, UseCardRequest(), authAs(owner))

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body?.description)
    }

    @Test
    fun `should delete card from repository after use`() {
        val card = cardRepository.save(com.houseclash.backend.domain.model.Card.create(owner.id!!, CardType.HOUSE_BONUS))

        controller.useCard(card.id!!, UseCardRequest(), authAs(owner))

        assertNull(cardRepository.findById(card.id))
    }

    @Test
    fun `should throw when card does not belong to the requesting user`() {
        val other = registerUserUsecase.execute("Other", "other@email.com", "Password1")
        joinHouseUsecase.execute(other.id!!, house.inviteCode)
        val card = cardRepository.save(com.houseclash.backend.domain.model.Card.create(other.id, CardType.HOUSE_BONUS))

        assertThrows(IllegalArgumentException::class.java) {
            controller.useCard(card.id!!, UseCardRequest(), authAs(owner))
        }
    }

    @Test
    fun `should throw when using a non-existent card`() {
        assertThrows(IllegalArgumentException::class.java) {
            controller.useCard(999L, UseCardRequest(), authAs(owner))
        }
    }

    @Test
    fun `should apply kudos to all house members when using HOUSE_BONUS card`() {
        val member = registerUserUsecase.execute("Member", "member@email.com", "Password1")
        joinHouseUsecase.execute(member.id!!, house.inviteCode)
        val card = cardRepository.save(com.houseclash.backend.domain.model.Card.create(owner.id!!, CardType.HOUSE_BONUS))

        controller.useCard(card.id!!, UseCardRequest(), authAs(owner))

        val updatedMember = userRepository.findById(member.id)!!
        assertTrue(updatedMember.kudosBalance > 0)
    }
}
