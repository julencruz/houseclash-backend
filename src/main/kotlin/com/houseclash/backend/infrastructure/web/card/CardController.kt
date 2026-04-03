package com.houseclash.backend.infrastructure.web.card

import com.houseclash.backend.domain.usecase.ExecuteCardEffectUsecase
import com.houseclash.backend.domain.usecase.GetUserCardsUsecase
import com.houseclash.backend.domain.usecase.OpenCardPackUsecase
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/cards")
class CardController(
    private val getUserCardsUsecase: GetUserCardsUsecase,
    private val openCardPackUsecase: OpenCardPackUsecase,
    private val executeCardEffectUsecase: ExecuteCardEffectUsecase
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun getMyCards(authentication: Authentication): ResponseEntity<List<CardResponse>> {
        val userId = authentication.principal as Long
        logger.info("User {} fetching their cards", userId)
        val cards = getUserCardsUsecase.execute(userId)
        logger.info("Returning {} cards for user {}", cards.size, userId)
        return ResponseEntity.ok(cards.map { it.toResponse() })
    }

    @PostMapping("/open-pack")
    fun openPack(authentication: Authentication): ResponseEntity<List<CardResponse>> {
        val userId = authentication.principal as Long
        logger.info("User {} opening a card pack", userId)
        val cards = openCardPackUsecase.execute(userId)
        logger.info("User {} received {} cards from pack", userId, cards.size)
        return ResponseEntity.ok(cards.map { it.toResponse() })
    }

    @PostMapping("/{cardId}/use")
    fun useCard(
        @PathVariable cardId: Long,
        @RequestBody request: UseCardRequest,
        authentication: Authentication
    ): ResponseEntity<CardEffectResultResponse> {
        val userId = authentication.principal as Long
        logger.info("User {} using card {} (targetUser={}, targetTask={}, targetCategory={})",
            userId, cardId, request.targetUserId, request.targetTaskId, request.targetCategoryId)
        val result = executeCardEffectUsecase.execute(
            cardId = cardId,
            executingUserId = userId,
            targetUserId = request.targetUserId,
            targetTaskId = request.targetTaskId,
            targetCategoryId = request.targetCategoryId
        )
        logger.info("Card {} effect executed by user {}", cardId, userId)
        return ResponseEntity.ok(result.toResponse())
    }
}
