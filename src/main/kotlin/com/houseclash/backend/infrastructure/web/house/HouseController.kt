package com.houseclash.backend.infrastructure.web.house

import com.houseclash.backend.domain.model.RankingPeriod
import com.houseclash.backend.domain.usecase.*
import com.houseclash.backend.infrastructure.web.user.UserResponse
import com.houseclash.backend.infrastructure.web.user.toResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/houses")
@Tag(name = "Llar", description = "Gestió de les llars, membres i rànquings")
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

    @Operation(summary = "Crear una llar", description = "Crea una nova llar amb l'usuari autenticat com a propietari. Genera automàticament un codi d'invitació únic")
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

    @Operation(summary = "Obtenir detalls de la meva llar", description = "Retorna la informació completa de la llar a la qual pertany l'usuari autenticat, incloent-hi els membres")
    @GetMapping("/me")
    fun getMyHouse(authentication: Authentication): ResponseEntity<HouseDetailsResponse> {
        val userId = authentication.principal as Long
        logger.info("User {} fetching their house details", userId)
        val details = getHouseDetailsUsecase.execute(userId)
        logger.info("Returning house {} details for user {}", details.house.id, userId)
        return ResponseEntity.ok(details.toResponse())
    }

    @Operation(summary = "Unir-se a una llar", description = "L'usuari autenticat s'uneix a una llar existent mitjançant el codi d'invitació")
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

    @Operation(summary = "Abandonar la llar", description = "L'usuari autenticat abandona la llar. Si és el propietari, ha de transferir la propietat abans de poder marxar")
    @PostMapping("/leave")
    fun leave(authentication: Authentication): ResponseEntity<Void> {
        val userId = authentication.principal as Long
        logger.info("User {} leaving their house", userId)
        leaveHouseUsecase.execute(userId)
        logger.info("User {} has left their house", userId)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "Expulsar un membre", description = "El propietari de la llar expulsa un membre. L'usuari expulsat perd l'accés a les tasques i categories de la llar")
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

    @Operation(summary = "Transferir la propietat de la llar", description = "El propietari actual cedeix la propietat de la llar a un altre membre. Aquesta acció és irreversible sense la cooperació del nou propietari")
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

    @Operation(summary = "Actualitzar la llar", description = "Modifica el nom de la llar. Només el propietari pot fer aquesta acció")
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

    @Operation(summary = "Obtenir el rànquing de la llar", description = "Retorna l'estadística de punts dels membres de la llar per al període indicat: WEEKLY, MONTHLY o ALL_TIME")
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
