package com.houseclash.backend.infrastructure.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SwaggerSecurityConfig {

    @Value("\${app.swagger.password}")
    private lateinit var swaggerPassword: String

    @Bean
    @Order(1)
    fun swaggerSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        val userDetails = User.withUsername("admin")
            .password("{noop}$swaggerPassword")
            .roles("SWAGGER")
            .build()

        val authManager = ProviderManager(
            DaoAuthenticationProvider(InMemoryUserDetailsManager(userDetails))
        )

        http
            .securityMatcher("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**")
            .authenticationManager(authManager)
            .authorizeHttpRequests { it.anyRequest().authenticated() }
            .httpBasic { }
            .csrf { it.disable() }

        return http.build()
    }
}
