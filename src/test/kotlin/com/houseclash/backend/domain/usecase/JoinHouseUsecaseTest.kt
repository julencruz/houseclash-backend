package com.houseclash.backend.domain.usecase

import com.houseclash.backend.helper.HouseRepositoryTester
import com.houseclash.backend.helper.PasswordEncoderTester
import com.houseclash.backend.helper.UserRepositoryTester
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class JoinHouseUsecaseTest {
    private val userRepository = UserRepositoryTester()
    private val houseRepository = HouseRepositoryTester()
    private val activityLogRepository = com.houseclash.backend.helper.ActivityLogRepositoryTester()
    private val registerUsecase = RegisterUserUsecase(userRepository, PasswordEncoderTester())
    private val createHouseUsecase = CreateHouseUsecase(userRepository, houseRepository)
    private val usecase = JoinHouseUsecase(userRepository, houseRepository, activityLogRepository)

    @Test
    fun `should join house correctly`() {
        val user = registerUsecase.execute("Test", "test@email.com", "Password1")
        val house = createHouseUsecase.execute(user.id!!, "Pis de Gràcia")
        val user2 = registerUsecase.execute("Test2", "test2@email.com", "Password2")
        val newUser = usecase.execute(user2.id!!, house.inviteCode)
        assertEquals(house.id, newUser.houseId)
    }

    @Test
    fun `should grant welcome bonus kudos on first visit to a house`() {
        val user = registerUsecase.execute("Test", "test@email.com", "Password1")
        val house = createHouseUsecase.execute(user.id!!, "Pis de Gràcia")
        val user2 = registerUsecase.execute("Test2", "test2@email.com", "Password2")

        val joined = usecase.execute(user2.id!!, house.inviteCode)

        assertEquals(JoinHouseUsecase.WELCOME_BONUS_KUDOS, joined.kudosBalance)
        assertTrue(joined.hasVisitedHouse(house.id!!))
    }

    @Test
    fun `should not grant welcome bonus kudos when rejoining the same house`() {
        val owner = registerUsecase.execute("Owner", "owner@email.com", "Password1")
        val house = createHouseUsecase.execute(owner.id!!, "Pis de Gràcia")
        val user = registerUsecase.execute("Test", "test@email.com", "Password1")

        // First join — gets bonus
        usecase.execute(user.id!!, house.inviteCode)

        // Simulate leave: reset houseId and kudos directly (owner stays so house still exists)
        val userAfterJoin = userRepository.findById(user.id)!!
        userRepository.save(userAfterJoin.leaveHouse(house.id!!))

        // Rejoin same house — no bonus
        val rejoined = usecase.execute(user.id, house.inviteCode)
        assertEquals(0, rejoined.kudosBalance)
    }

    @Test
    fun `should grant welcome bonus kudos when joining a different house for the first time`() {
        val owner1 = registerUsecase.execute("Owner1", "o1@email.com", "Password1")
        val owner2 = registerUsecase.execute("Owner2", "o2@email.com", "Password2")
        val house1 = createHouseUsecase.execute(owner1.id!!, "Casa A")
        val house2 = createHouseUsecase.execute(owner2.id!!, "Casa B")
        val user = registerUsecase.execute("Test", "test@email.com", "Password1")

        // Join house1 — gets bonus
        usecase.execute(user.id!!, house1.inviteCode)

        // Simulate leave house1 directly (owner1 stays so house1 still exists)
        val userAfterJoin = userRepository.findById(user.id)!!
        userRepository.save(userAfterJoin.leaveHouse(house1.id!!))

        // Join house2 for the first time — should get bonus again
        val joinedHouse2 = usecase.execute(user.id, house2.inviteCode)
        assertEquals(JoinHouseUsecase.WELCOME_BONUS_KUDOS, joinedHouse2.kudosBalance)
        assertTrue(joinedHouse2.hasVisitedHouse(house2.id!!))
    }

    @Test
    fun `should throw because user already has house`() {
        val user = registerUsecase.execute("Test", "test@email.com", "Password1")
        val house = createHouseUsecase.execute(user.id!!, "Pis de Gràcia")
        val user2 = registerUsecase.execute("Test2", "test2@email.com", "Password2")
        createHouseUsecase.execute(user2.id!!, "Carrer Mallorca")
        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(user.id, house.inviteCode)
        }
    }

    @Test
    fun `should throw because house doesnt exist`() {
        val user = registerUsecase.execute("Test2", "test2@email.com", "Password2")
        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(user.id!!, "FAKEID")
        }
    }
}
