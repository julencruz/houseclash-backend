package com.houseclash.backend.infrastructure.web.user

import com.houseclash.backend.domain.usecase.LoginUserUsecase
import com.houseclash.backend.domain.usecase.RegisterUserUsecase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val registerUserUsecase: RegisterUserUsecase,
    private val loginUserUsecase: LoginUserUsecase
) {

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<UserResponse> {
        val user = registerUserUsecase.execute(
            username = request.username,
            email = request.email,
            rawPassword = request.passwordRaw
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(user.toResponse())
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<UserResponse> {
        val user = loginUserUsecase.execute(
            email = request.email,
            password = request.passwordRaw,
        )
        return ResponseEntity.ok(user.toResponse())
    }
}
