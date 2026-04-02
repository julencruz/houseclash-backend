package com.houseclash.backend.infrastructure.web.card

import com.houseclash.backend.domain.usecase.ExecuteCardEffectUsecase
import com.houseclash.backend.domain.usecase.GetUserCardsUsecase
import com.houseclash.backend.domain.usecase.OpenCardPackUsecase
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

    @GetMapping
    fun getMyCards(authentication: Authentication): ResponseEntity<List<CardResponse>> {
        val userId = authentication.principal as Long
        val cards = getUserCardsUsecase.execute(userId)
        return ResponseEntity.ok(cards.map { it.toResponse() })
    }

    @PostMapping("/open-pack")
    fun openPack(authentication: Authentication): ResponseEntity<List<CardResponse>> {
        val userId = authentication.principal as Long
        val cards = openCardPackUsecase.execute(userId)
        return ResponseEntity.ok(cards.map { it.toResponse() })
    }

    @PostMapping("/{cardId}/use")
    fun useCard(
        @PathVariable cardId: Long,
        @RequestBody request: UseCardRequest,
        authentication: Authentication
    ): ResponseEntity<CardEffectResultResponse> {
        val userId = authentication.principal as Long
        val result = executeCardEffectUsecase.execute(
            cardId = cardId,
            executingUserId = userId,
            targetUserId = request.targetUserId,
            targetTaskId = request.targetTaskId,
            targetCategoryId = request.targetCategoryId
        )
        return ResponseEntity.ok(result.toResponse())
    }
}
