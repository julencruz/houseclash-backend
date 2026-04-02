// infrastructure/web/house/HouseController.kt
package com.houseclash.backend.infrastructure.web.house

import com.houseclash.backend.domain.model.RankingPeriod
import com.houseclash.backend.domain.usecase.*
import com.houseclash.backend.infrastructure.web.user.UserResponse
import com.houseclash.backend.infrastructure.web.user.toResponse
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

    @PostMapping
    fun create(
        @RequestBody request: CreateHouseRequest,
        authentication: Authentication
    ): ResponseEntity<HouseResponse> {
        val userId = authentication.principal as Long
        val house = createHouseUsecase.execute(userId, request.name, request.description)
        return ResponseEntity.status(HttpStatus.CREATED).body(house.toResponse())
    }

    @GetMapping("/me")
    fun getMyHouse(authentication: Authentication): ResponseEntity<HouseDetailsResponse> {
        val userId = authentication.principal as Long
        val details = getHouseDetailsUsecase.execute(userId)
        return ResponseEntity.ok(details.toResponse())
    }

    @PostMapping("/join")
    fun join(
        @RequestBody request: JoinHouseRequest,
        authentication: Authentication
    ): ResponseEntity<UserResponse> {
        val userId = authentication.principal as Long
        val user = joinHouseUsecase.execute(userId, request.inviteCode)
        return ResponseEntity.ok(user.toResponse())
    }

    @PostMapping("/leave")
    fun leave(authentication: Authentication): ResponseEntity<Void> {
        val userId = authentication.principal as Long
        leaveHouseUsecase.execute(userId)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/kick")
    fun kick(
        @RequestBody request: KickMemberRequest,
        authentication: Authentication
    ): ResponseEntity<UserResponse> {
        val userId = authentication.principal as Long
        val kicked = kickMemberUsecase.execute(userId, request.userId)
        return ResponseEntity.ok(kicked.toResponse())
    }

    @PostMapping("/transfer-ownership")
    fun transferOwnership(
        @RequestBody request: TransferOwnershipRequest,
        authentication: Authentication
    ): ResponseEntity<HouseResponse> {
        val userId = authentication.principal as Long
        val house = transferHouseOwnershipUsecase.execute(userId, request.newOwnerId)
        return ResponseEntity.ok(house.toResponse())
    }

    @PatchMapping
    fun update(
        @RequestBody request: UpdateHouseRequest,
        authentication: Authentication
    ): ResponseEntity<HouseResponse> {
        val userId = authentication.principal as Long
        val user = getHouseDetailsUsecase.execute(userId)
        val house = updateHouseUsecase.execute(userId, user.house.id!!, request.name)
        return ResponseEntity.ok(house.toResponse())
    }

    @GetMapping("/ranking")
    fun getRanking(
        @RequestParam period: RankingPeriod,
        authentication: Authentication
    ): ResponseEntity<List<MemberStatsResponse>> {
        val userId = authentication.principal as Long
        val details = getHouseDetailsUsecase.execute(userId)
        val ranking = getHouseRankingUsecase.execute(details.house.id!!, period)
        return ResponseEntity.ok(ranking.map { it.toResponse() })
    }
}
