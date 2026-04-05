package com.houseclash.backend.infrastructure.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.houseclash.backend.infrastructure.web.ErrorResponse
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import java.time.LocalDateTime

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthFilter: JwtAuthFilter
) {

    private val mapper = ObjectMapper().registerModule(JavaTimeModule())

    private fun writeError(response: HttpServletResponse, status: HttpStatus, error: String, message: String) {
        response.status = status.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        val body = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = status.value(),
            error = error,
            message = message
        )
        response.writer.write(mapper.writeValueAsString(body))
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers(
                    "/api/users/register", "/api/users/login",
                    "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/webjars/**"
                ).permitAll()
                auth.anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .exceptionHandling { ex ->
                // 401 - No token or invalid token
                ex.authenticationEntryPoint { _, response, _ ->
                    writeError(response, HttpStatus.UNAUTHORIZED, "Unauthorized", "Authentication required. Please provide a valid token.")
                }
                // 403 - Valid token but insufficient permissions
                ex.accessDeniedHandler { _, response, _ ->
                    writeError(response, HttpStatus.FORBIDDEN, "Forbidden", "You do not have permission to perform this action")
                }
            }

        return http.build()
    }
}
