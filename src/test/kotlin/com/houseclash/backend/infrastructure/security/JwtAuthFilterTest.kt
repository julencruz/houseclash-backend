package com.houseclash.backend.infrastructure.security

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder

class JwtAuthFilterTest {

    private val jwtService = JwtService("houseclash-test-secret-key-for-unit-tests-only")
    private val filter = JwtAuthFilter(jwtService)

    @BeforeEach
    fun setUp() {
        SecurityContextHolder.clearContext()
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `should set authentication when valid token is provided`() {
        val token = jwtService.generateToken(1L)
        val request = MockHttpServletRequest().apply {
            addHeader("Authorization", "Bearer $token")
        }

        filter.doFilter(request, MockHttpServletResponse(), MockFilterChain())

        val auth = SecurityContextHolder.getContext().authentication
        assertNotNull(auth)
        assertEquals(1L, auth?.principal)
    }

    @Test
    fun `should set correct userId in authentication principal`() {
        val token = jwtService.generateToken(77L)
        val request = MockHttpServletRequest().apply {
            addHeader("Authorization", "Bearer $token")
        }

        filter.doFilter(request, MockHttpServletResponse(), MockFilterChain())

        val auth = SecurityContextHolder.getContext().authentication
        assertEquals(77L, auth?.principal)
    }

    @Test
    fun `should not set authentication when Authorization header is missing`() {
        filter.doFilter(MockHttpServletRequest(), MockHttpServletResponse(), MockFilterChain())

        assertNull(SecurityContextHolder.getContext().authentication)
    }

    @Test
    fun `should not set authentication when header uses Basic scheme`() {
        val request = MockHttpServletRequest().apply {
            addHeader("Authorization", "Basic somebase64token")
        }

        filter.doFilter(request, MockHttpServletResponse(), MockFilterChain())

        assertNull(SecurityContextHolder.getContext().authentication)
    }

    @Test
    fun `should not set authentication when token is invalid`() {
        val request = MockHttpServletRequest().apply {
            addHeader("Authorization", "Bearer invalid.token.value")
        }

        filter.doFilter(request, MockHttpServletResponse(), MockFilterChain())

        assertNull(SecurityContextHolder.getContext().authentication)
    }

    @Test
    fun `should not set authentication when token is empty after Bearer prefix`() {
        val request = MockHttpServletRequest().apply {
            addHeader("Authorization", "Bearer ")
        }

        filter.doFilter(request, MockHttpServletResponse(), MockFilterChain())

        assertNull(SecurityContextHolder.getContext().authentication)
    }

    @Test
    fun `should always continue the filter chain even without token`() {
        val chain = MockFilterChain()

        filter.doFilter(MockHttpServletRequest(), MockHttpServletResponse(), chain)

        assertNotNull(chain.request)
    }

    @Test
    fun `should always continue the filter chain with a valid token`() {
        val token = jwtService.generateToken(1L)
        val request = MockHttpServletRequest().apply {
            addHeader("Authorization", "Bearer $token")
        }
        val chain = MockFilterChain()

        filter.doFilter(request, MockHttpServletResponse(), chain)

        assertNotNull(chain.request)
    }
}
