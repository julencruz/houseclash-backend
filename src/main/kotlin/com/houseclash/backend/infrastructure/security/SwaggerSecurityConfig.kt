package com.houseclash.backend.infrastructure.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.Base64

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class SwaggerBasicAuthFilter : OncePerRequestFilter() {

    @Value("\${app.swagger.password}")
    private lateinit var swaggerPassword: String

    private val swaggerPaths = listOf("/swagger-ui", "/v3/api-docs", "/webjars", "/api-docs")

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val uri = request.requestURI
        val isSwaggerPath = uri == "/swagger-ui.html" || uri == "/api-docs" || swaggerPaths.any { uri.startsWith(it) }

        if (!isSwaggerPath) {
            filterChain.doFilter(request, response)
            return
        }

        val authHeader = request.getHeader("Authorization")
        if (authHeader != null && authHeader.startsWith("Basic ")) {
            val decoded = String(Base64.getDecoder().decode(authHeader.substring(6)))
            val colonIndex = decoded.indexOf(':')
            if (colonIndex > 0) {
                val username = decoded.substring(0, colonIndex)
                val password = decoded.substring(colonIndex + 1)
                if (username == "admin" && password == swaggerPassword) {
                    filterChain.doFilter(request, response)
                    return
                }
            }
        }

        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.setHeader("WWW-Authenticate", "Basic realm=\"HouseClash Swagger\"")
    }
}
