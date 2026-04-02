package com.houseclash.backend.infrastructure.security

import com.houseclash.backend.domain.port.PasswordEncoder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class BCryptPasswordEncoderAdapter : PasswordEncoder {
    private val encoder = BCryptPasswordEncoder()

    override fun encode(rawPassword: String): String = encoder.encode(rawPassword)!!

    override fun matches(rawPassword: String, encodedPassword: String): Boolean =
        encoder.matches(rawPassword, encodedPassword)
}
