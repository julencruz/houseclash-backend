package com.houseclash.backend.infrastructure.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date

@Service
class JwtService(
    @Value("\${app.jwt.secret}")
    private val secret: String
) {
    private val secretKey by lazy { Keys.hmacShaKeyFor(secret.toByteArray()) }

    fun generateToken(userId: Long): String {
        return Jwts.builder()
            .subject(userId.toString())
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
            .signWith(secretKey)
            .compact()
    }

    fun extractUserId(token: String): Long {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
            .subject
            .toLong()
    }

    fun isTokenValid(token: String): Boolean {
        return try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token)
            true
        } catch (e: Exception) {
            false
        }
    }
}
