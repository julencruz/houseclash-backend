package com.houseclash.backend.domain.model

import com.houseclash.backend.domain.model.cardlogic.CardEffect
import com.houseclash.backend.domain.model.cardlogic.effect.CategoryBoostEffect
import com.houseclash.backend.domain.model.cardlogic.effect.CleanSlateEffect
import com.houseclash.backend.domain.model.cardlogic.effect.ForceTaskEffect
import com.houseclash.backend.domain.model.cardlogic.effect.GracePeriodEffect
import com.houseclash.backend.domain.model.cardlogic.effect.HouseBonusEffect
import com.houseclash.backend.domain.model.cardlogic.effect.MarketBoostEffect
import com.houseclash.backend.domain.model.cardlogic.effect.SkipTaskEffect
import com.houseclash.backend.domain.model.cardlogic.effect.StealKudosEffect
import com.houseclash.backend.domain.model.cardlogic.effect.UnderdogBoostEffect
import com.houseclash.backend.domain.model.cardlogic.effect.ValueInflationEffect
import java.time.LocalDateTime

data class Card (
    val id: Long? = null,
    val userId: Long,
    val type: CardType,
    val acquiredAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun create(userId: Long, cardType: CardType): Card {
            return Card(
                userId = userId,
                type = cardType
            )
        }
    }
}

enum class CardType(val probability: Int, val effect: CardEffect) {

    // -- COLLABORATIVE --
    HOUSE_BONUS(10, HouseBonusEffect()),
    MARKET_BOOST(10, MarketBoostEffect()),
    GRACE_PERIOD(10, GracePeriodEffect()),
    UNDERDOG_BOOST(10, UnderdogBoostEffect()),
    CATEGORY_BOOST(10, CategoryBoostEffect()),

    // -- INDIVIDUAL ADVANTAGE --
    CLEAN_SLATE(10, CleanSlateEffect()),
    SKIP_TASK(10, SkipTaskEffect()),
    VALUE_INFLATION(10, ValueInflationEffect()),

    // -- OFFENSIVE --
    FORCE_TASK(10, ForceTaskEffect()),
    STEAL_KUDOS(10, StealKudosEffect())
}
