package com.houseclash.backend.infrastructure.web.house

import com.houseclash.backend.domain.model.RankingPeriod
import com.houseclash.backend.domain.usecase.*
import com.houseclash.backend.helper.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

class HouseControllerTest {

    private val userRepository = UserRepositoryTester()
    private val houseRepository = HouseRepositoryTester()
    private val taskRepository = TaskRepositoryTester()
    private val cardRepository = CardRepositoryTester()
    private val categoryRepository = CategoryRepositoryTester()
    private val passwordEncoder = PasswordEncoderTester()
    private val activityLogRepository = ActivityLogRepositoryTester()

    private val registerUserUsecase = RegisterUserUsecase(userRepository, passwordEncoder)
    private val createHouseUsecase = CreateHouseUsecase(userRepository, houseRepository)
    private val getHouseDetailsUsecase = GetHouseDetailsUsecase(userRepository, houseRepository)
    private val joinHouseUsecase = JoinHouseUsecase(userRepository, houseRepository, activityLogRepository)
    private val leaveHouseUsecase = LeaveHouseUsecase(userRepository, taskRepository, cardRepository, houseRepository, categoryRepository)
    private val kickMemberUsecase = KickMemberUsecase(userRepository, houseRepository, taskRepository, cardRepository, activityLogRepository)
    private val transferHouseOwnershipUsecase = TransferHouseOwnershipUsecase(houseRepository, userRepository, activityLogRepository)
    private val updateHouseUsecase = UpdateHouseUsecase(houseRepository, userRepository)
    private val getHouseRankingUsecase = GetHouseRankingUsecase(userRepository, taskRepository)

    private val controller = HouseController(
        createHouseUsecase,
        getHouseDetailsUsecase,
        joinHouseUsecase,
        leaveHouseUsecase,
        kickMemberUsecase,
        transferHouseOwnershipUsecase,
        updateHouseUsecase,
        getHouseRankingUsecase
    )

    // ---- create ----

    @Test
    fun `should return 201 with house data when creating a house`() {
        val owner = registerUserUsecase.execute("Owner", "owner@email.com", "Password1")
        val auth = UsernamePasswordAuthenticationToken(owner.id!!, null, emptyList())

        val response = controller.create(CreateHouseRequest("My House"), auth)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals("My House", response.body?.name)
        assertNotNull(response.body?.inviteCode)
    }

    @Test
    fun `should throw when user already belongs to a house and tries to create another`() {
        val owner = registerUserUsecase.execute("Owner", "owner@email.com", "Password1")
        val auth = UsernamePasswordAuthenticationToken(owner.id!!, null, emptyList())
        controller.create(CreateHouseRequest("First House"), auth)

        assertThrows(IllegalArgumentException::class.java) {
            controller.create(CreateHouseRequest("Second House"), auth)
        }
    }

    // ---- getMyHouse ----

    @Test
    fun `should return 200 with house details for user in a house`() {
        val owner = registerUserUsecase.execute("Owner", "owner@email.com", "Password1")
        createHouseUsecase.execute(owner.id!!, "My House")
        val auth = UsernamePasswordAuthenticationToken(owner.id, null, emptyList())

        val response = controller.getMyHouse(auth)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("My House", response.body?.house?.name)
        assertEquals(1, response.body?.members?.size)
    }

    @Test
    fun `should throw when user does not belong to any house`() {
        val user = registerUserUsecase.execute("User", "user@email.com", "Password1")
        val auth = UsernamePasswordAuthenticationToken(user.id!!, null, emptyList())

        assertThrows(IllegalArgumentException::class.java) {
            controller.getMyHouse(auth)
        }
    }

    // ---- join ----

    @Test
    fun `should return 200 and user with houseId after joining`() {
        val owner = registerUserUsecase.execute("Owner", "owner@email.com", "Password1")
        val house = createHouseUsecase.execute(owner.id!!, "My House")
        val member = registerUserUsecase.execute("Member", "member@email.com", "Password1")
        val auth = UsernamePasswordAuthenticationToken(member.id!!, null, emptyList())

        val response = controller.join(JoinHouseRequest(house.inviteCode), auth)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(house.id, response.body?.houseId)
    }

    @Test
    fun `should throw when joining with an invalid invite code`() {
        val member = registerUserUsecase.execute("Member", "member@email.com", "Password1")
        val auth = UsernamePasswordAuthenticationToken(member.id!!, null, emptyList())

        assertThrows(IllegalArgumentException::class.java) {
            controller.join(JoinHouseRequest("BADCOD"), auth)
        }
    }

    @Test
    fun `should throw when user already belongs to a house and tries to join another`() {
        val owner = registerUserUsecase.execute("Owner", "owner@email.com", "Password1")
        val house = createHouseUsecase.execute(owner.id!!, "My House")
        val auth = UsernamePasswordAuthenticationToken(owner.id, null, emptyList())

        assertThrows(IllegalArgumentException::class.java) {
            controller.join(JoinHouseRequest(house.inviteCode), auth)
        }
    }

    // ---- leave ----

    @Test
    fun `should return 204 when a non-captain member leaves the house`() {
        val owner = registerUserUsecase.execute("Owner", "owner@email.com", "Password1")
        val house = createHouseUsecase.execute(owner.id!!, "My House")
        val member = registerUserUsecase.execute("Member", "member@email.com", "Password1")
        joinHouseUsecase.execute(member.id!!, house.inviteCode)
        val auth = UsernamePasswordAuthenticationToken(member.id, null, emptyList())

        val response = controller.leave(auth)

        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
    }

    @Test
    fun `should throw when captain tries to leave with other members present`() {
        val owner = registerUserUsecase.execute("Owner", "owner@email.com", "Password1")
        val house = createHouseUsecase.execute(owner.id!!, "My House")
        val member = registerUserUsecase.execute("Member", "member@email.com", "Password1")
        joinHouseUsecase.execute(member.id!!, house.inviteCode)
        val auth = UsernamePasswordAuthenticationToken(owner.id, null, emptyList())

        assertThrows(IllegalArgumentException::class.java) {
            controller.leave(auth)
        }
    }

    // ---- kick ----

    @Test
    fun `should return 200 and kicked user with null houseId`() {
        val owner = registerUserUsecase.execute("Owner", "owner@email.com", "Password1")
        val house = createHouseUsecase.execute(owner.id!!, "My House")
        val member = registerUserUsecase.execute("Member", "member@email.com", "Password1")
        joinHouseUsecase.execute(member.id!!, house.inviteCode)
        val auth = UsernamePasswordAuthenticationToken(owner.id, null, emptyList())

        val response = controller.kick(KickMemberRequest(member.id), auth)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNull(response.body?.houseId)
    }

    @Test
    fun `should throw when a non-captain tries to kick a member`() {
        val owner = registerUserUsecase.execute("Owner", "owner@email.com", "Password1")
        val house = createHouseUsecase.execute(owner.id!!, "My House")
        val member = registerUserUsecase.execute("Member", "member@email.com", "Password1")
        joinHouseUsecase.execute(member.id!!, house.inviteCode)
        val auth = UsernamePasswordAuthenticationToken(member.id, null, emptyList())

        assertThrows(IllegalArgumentException::class.java) {
            controller.kick(KickMemberRequest(owner.id), auth)
        }
    }

    // ---- transferOwnership ----

    @Test
    fun `should return 200 with house showing new captain`() {
        val owner = registerUserUsecase.execute("Owner", "owner@email.com", "Password1")
        val house = createHouseUsecase.execute(owner.id!!, "My House")
        val member = registerUserUsecase.execute("Member", "member@email.com", "Password1")
        joinHouseUsecase.execute(member.id!!, house.inviteCode)
        val auth = UsernamePasswordAuthenticationToken(owner.id, null, emptyList())

        val response = controller.transferOwnership(TransferOwnershipRequest(member.id), auth)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(member.id, response.body?.createdBy)
    }

    @Test
    fun `should throw when non-captain tries to transfer ownership`() {
        val owner = registerUserUsecase.execute("Owner", "owner@email.com", "Password1")
        val house = createHouseUsecase.execute(owner.id!!, "My House")
        val member = registerUserUsecase.execute("Member", "member@email.com", "Password1")
        joinHouseUsecase.execute(member.id!!, house.inviteCode)
        val auth = UsernamePasswordAuthenticationToken(member.id, null, emptyList())

        assertThrows(IllegalArgumentException::class.java) {
            controller.transferOwnership(TransferOwnershipRequest(owner.id), auth)
        }
    }

    // ---- update ----

    @Test
    fun `should return 200 with updated house name`() {
        val owner = registerUserUsecase.execute("Owner", "owner@email.com", "Password1")
        createHouseUsecase.execute(owner.id!!, "Old Name")
        val auth = UsernamePasswordAuthenticationToken(owner.id, null, emptyList())

        val response = controller.update(UpdateHouseRequest("New Name"), auth)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("New Name", response.body?.name)
    }

    @Test
    fun `should throw when non-captain tries to update the house`() {
        val owner = registerUserUsecase.execute("Owner", "owner@email.com", "Password1")
        val house = createHouseUsecase.execute(owner.id!!, "My House")
        val member = registerUserUsecase.execute("Member", "member@email.com", "Password1")
        joinHouseUsecase.execute(member.id!!, house.inviteCode)
        val auth = UsernamePasswordAuthenticationToken(member.id, null, emptyList())

        assertThrows(IllegalArgumentException::class.java) {
            controller.update(UpdateHouseRequest("Hijacked Name"), auth)
        }
    }

    // ---- getRanking ----

    @Test
    fun `should return 200 with ranking list containing the house members`() {
        val owner = registerUserUsecase.execute("Owner", "owner@email.com", "Password1")
        createHouseUsecase.execute(owner.id!!, "My House")
        val auth = UsernamePasswordAuthenticationToken(owner.id, null, emptyList())

        val response = controller.getRanking(RankingPeriod.ALL_TIME, auth)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(1, response.body?.size)
        assertEquals(1, response.body?.first()?.rank)
    }

    @Test
    fun `should return ranking sorted by kudos balance descending`() {
        val owner = registerUserUsecase.execute("Owner", "owner@email.com", "Password1")
        val house = createHouseUsecase.execute(owner.id!!, "My House")
        val member = registerUserUsecase.execute("Member", "member@email.com", "Password1")
        joinHouseUsecase.execute(member.id!!, house.inviteCode)
        val auth = UsernamePasswordAuthenticationToken(owner.id, null, emptyList())

        val response = controller.getRanking(RankingPeriod.ALL_TIME, auth)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(2, response.body?.size)
        val ranks = response.body!!.map { it.rank }
        assertEquals(listOf(1, 2), ranks)
    }
}
