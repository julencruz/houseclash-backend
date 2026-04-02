// infrastructure/web/house/HouseDTOs.kt
package com.houseclash.backend.infrastructure.web.house

import com.houseclash.backend.domain.model.House
import com.houseclash.backend.domain.model.HouseDetails
import com.houseclash.backend.domain.model.MemberStats
import com.houseclash.backend.infrastructure.web.user.UserResponse
import com.houseclash.backend.infrastructure.web.user.toResponse
import java.time.LocalDateTime

// --- REQUESTS ---

data class CreateHouseRequest(
    val name: String,
    val description: String = ""
)

data class JoinHouseRequest(
    val inviteCode: String
)

data class UpdateHouseRequest(
    val name: String
)

data class TransferOwnershipRequest(
    val newOwnerId: Long
)

data class KickMemberRequest(
    val userId: Long
)

// --- RESPONSES ---

data class HouseResponse(
    val id: Long,
    val name: String,
    val description: String,
    val inviteCode: String,
    val createdBy: Long,
    val createdAt: LocalDateTime
)

data class HouseDetailsResponse(
    val house: HouseResponse,
    val members: List<UserResponse>
)

data class MemberStatsResponse(
    val user: UserResponse,
    val kudosBalance: Int,
    val tasksCompleted: Int,
    val rank: Int
)

// --- MAPPERS ---

fun House.toResponse() = HouseResponse(
    id = this.id!!,
    name = this.name,
    description = this.description,
    inviteCode = this.inviteCode,
    createdBy = this.createdBy,
    createdAt = this.createdAt
)

fun HouseDetails.toResponse() = HouseDetailsResponse(
    house = this.house.toResponse(),
    members = this.members.map { it.toResponse() }
)

fun MemberStats.toResponse() = MemberStatsResponse(
    user = this.user.toResponse(),
    kudosBalance = this.kudosBalance,
    tasksCompleted = this.tasksCompleted,
    rank = this.rank
)
