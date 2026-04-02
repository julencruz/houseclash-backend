package com.houseclash.backend.domain.model

data class MemberStats(
    val user: User,
    val kudosBalance: Int,
    val tasksCompleted: Int,
    val rank: Int,
)

enum class RankingPeriod {
    WEEK,
    MONTH,
    ALL_TIME,
}
