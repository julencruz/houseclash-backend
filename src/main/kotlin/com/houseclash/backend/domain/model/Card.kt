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
    HOUSE_BONUS(10, HouseBonusEffect()),       // Adds +2 Kudos to all housemates.
    MARKET_BOOST(10, MarketBoostEffect()),      // Adds +1 Kudos to all OPEN tasks.
    GRACE_PERIOD(10, GracePeriodEffect()),      // Resets 24h validation timers for pending tasks.
    UNDERDOG_BOOST(10, UnderdogBoostEffect()),  // Adds +3 Kudos to the lowest value OPEN task.
    CATEGORY_BOOST(10, CategoryBoostEffect()),  // Adds +1 Kudos to all tasks in a selected category (assigned or not).

    // -- INDIVIDUAL ADVANTAGE --
    CLEAN_SLATE(10, CleanSlateEffect()),        // Frees one of your forced tasks back to OPEN.
    SKIP_TASK(10, SkipTaskEffect()),            // Drops an ASSIGNED task without the -2 Kudos penalty.
    VALUE_INFLATION(10, ValueInflationEffect()), // Multiplies an OPEN task's Kudos value by 2.

    // -- OFFENSIVE --
    FORCE_TASK(10, ForceTaskEffect()),          // Forces an OPEN task onto a specific housemate.
    STEAL_KUDOS(10, StealKudosEffect())         // Steals Kudos from a target housemate.
}
