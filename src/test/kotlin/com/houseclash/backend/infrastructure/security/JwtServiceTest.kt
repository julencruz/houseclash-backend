package com.houseclash.backend.infrastructure.security

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class JwtServiceTest {

    private val jwtService = JwtService()

    @Test
    fun `should generate a non-blank token`() {
        val token = jwtService.generateToken(1L)
        assertTrue(token.isNotBlank())
    }

    @Test
    fun `should extract userId from generated token`() {
        val token = jwtService.generateToken(42L)
        val userId = jwtService.extractUserId(token)
        assertEquals(42L, userId)
    }

    @Test
    fun `should extract correct userId for different users`() {
        val tokenA = jwtService.generateToken(1L)
        val tokenB = jwtService.generateToken(99L)
        assertEquals(1L, jwtService.extractUserId(tokenA))
        assertEquals(99L, jwtService.extractUserId(tokenB))
    }

    @Test
    fun `should validate a freshly generated token as valid`() {
        val token = jwtService.generateToken(1L)
        assertTrue(jwtService.isTokenValid(token))
    }

    @Test
    fun `should return false for a tampered token`() {
        val token = jwtService.generateToken(1L)
        val tampered = token.dropLast(5) + "XXXXX"
        assertFalse(jwtService.isTokenValid(tampered))
    }

    @Test
    fun `should return false for a completely invalid token`() {
        assertFalse(jwtService.isTokenValid("invalid.token.string"))
    }

    @Test
    fun `should return false for an empty string`() {
        assertFalse(jwtService.isTokenValid(""))
    }

    @Test
    fun `should generate different tokens for different users`() {
        val tokenA = jwtService.generateToken(1L)
        val tokenB = jwtService.generateToken(2L)
        assertNotEquals(tokenA, tokenB)
    }
}
