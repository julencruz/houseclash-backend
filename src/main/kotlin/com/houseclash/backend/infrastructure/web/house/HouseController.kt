// infrastructure/web/house/HouseController.kt
package com.houseclash.backend.infrastructure.web.house

import com.houseclash.backend.domain.model.RankingPeriod
import com.houseclash.backend.domain.usecase.*
import com.houseclash.backend.infrastructure.web.user.UserResponse
import com.houseclash.backend.infrastructure.web.user.toResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/houses")
class HouseController(
    private val createHouseUsecase: CreateHouseUsecase,
    private val getHouseDetailsUsecase: GetHouseDetailsUsecase,
    private val joinHouseUsecase: JoinHouseUsecase,
    private val leaveHouseUsecase: LeaveHouseUsecase,
    private val kickMemberUsecase: KickMemberUsecase,
    private val transferHouseOwnershipUsecase: TransferHouseOwnershipUsecase,
    private val updateHouseUsecase: UpdateHouseUsecase,
    private val getHouseRankingUsecase: GetHouseRankingUsecase,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping
    fun create(
        @RequestBody request: CreateHouseRequest,
        authentication: Authentication
    ): ResponseEntity<HouseResponse> {
        val userId = authentication.principal as Long
        logger.info("User {} creating house '{}'", userId, request.name)
        val house = createHouseUsecase.execute(userId, request.name, request.description)
        logger.info("House {} created by user {}", house.id, userId)
        return ResponseEntity.status(HttpStatus.CREATED).body(house.toResponse())
    }

    @GetMapping("/me")
    fun getMyHouse(authentication: Authentication): ResponseEntity<HouseDetailsResponse> {
        val userId = authentication.principal as Long
        logger.info("User {} fetching their house details", userId)
        val details = getHouseDetailsUsecase.execute(userId)
        logger.info("Returning house {} details for user {}", details.house.id, userId)
        return ResponseEntity.ok(details.toResponse())
    }

    @PostMapping("/join")
    fun join(
        @RequestBody request: JoinHouseRequest,
        authentication: Authentication
    ): ResponseEntity<UserResponse> {
        val userId = authentication.principal as Long
        logger.info("User {} joining house with invite code '{}'", userId, request.inviteCode)
        val user = joinHouseUsecase.execute(userId, request.inviteCode)
        logger.info("User {} successfully joined a house", userId)
        return ResponseEntity.ok(user.toResponse())
    }

    @PostMapping("/leave")
    fun leave(authentication: Authentication): ResponseEntity<Void> {
        val userId = authentication.principal as Long
        logger.info("User {} leaving their house", userId)
        leaveHouseUsecase.execute(userId)
        logger.info("User {} has left their house", userId)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/kick")
    fun kick(
        @RequestBody request: KickMemberRequest,
        authentication: Authentication
    ): ResponseEntity<UserResponse> {
        val userId = authentication.principal as Long
        logger.info("User {} kicking member {}", userId, request.userId)
        val kicked = kickMemberUsecase.execute(userId, request.userId)
        logger.info("Member {} kicked by user {}", request.userId, userId)
        return ResponseEntity.ok(kicked.toResponse())
    }

    @PostMapping("/transfer-ownership")
    fun transferOwnership(
        @RequestBody request: TransferOwnershipRequest,
        authentication: Authentication
    ): ResponseEntity<HouseResponse> {
        val userId = authentication.principal as Long
        logger.info("User {} transferring house ownership to user {}", userId, request.newOwnerId)
        val house = transferHouseOwnershipUsecase.execute(userId, request.newOwnerId)
        logger.info("House {} ownership transferred to user {}", house.id, request.newOwnerId)
        return ResponseEntity.ok(house.toResponse())
    }

    @PatchMapping
    fun update(
        @RequestBody request: UpdateHouseRequest,
        authentication: Authentication
    ): ResponseEntity<HouseResponse> {
        val userId = authentication.principal as Long
        logger.info("User {} updating their house", userId)
        val user = getHouseDetailsUsecase.execute(userId)
        val house = updateHouseUsecase.execute(userId, user.house.id!!, request.name)
        logger.info("House {} updated by user {}", house.id, userId)
        return ResponseEntity.ok(house.toResponse())
    }

    @GetMapping("/ranking")
    fun getRanking(
        @RequestParam period: RankingPeriod,
        authentication: Authentication
    ): ResponseEntity<List<MemberStatsResponse>> {
        val userId = authentication.principal as Long
        logger.info("User {} fetching house ranking for period {}", userId, period)
        val details = getHouseDetailsUsecase.execute(userId)
        val ranking = getHouseRankingUsecase.execute(details.house.id!!, period)
        logger.info("Returning {} ranked members for house {}", ranking.size, details.house.id)
        return ResponseEntity.ok(ranking.map { it.toResponse() })
    }
}
