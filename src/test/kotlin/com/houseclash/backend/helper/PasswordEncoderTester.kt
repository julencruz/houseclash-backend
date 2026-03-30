package com.houseclash.backend.helper

import com.houseclash.backend.domain.port.PasswordEncoder

class PasswordEncoderTester : PasswordEncoder {
    override fun encode(rawPassword: String): String {
        return "hashed_$rawPassword"
    }

    override fun matches(rawPassword: String, encodedPassword: String): Boolean {
        return encodedPassword == encode(rawPassword)
    }
}