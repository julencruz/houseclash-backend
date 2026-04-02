package com.houseclash.backend.infrastructure.web.user

import com.houseclash.backend.domain.usecase.LoginUserUsecase
import com.houseclash.backend.domain.usecase.RegisterUserUsecase
import com.houseclash.backend.infrastructure.security.JwtService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val registerUserUsecase: RegisterUserUsecase,
    private val loginUserUsecase: LoginUserUsecase,
    private val jwtService: JwtService
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
    fun login(@RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        val user = loginUserUsecase.execute(
            email = request.email,
            password = request.passwordRaw,
        )
        val token = jwtService.generateToken(user.id!!)
        return ResponseEntity.ok(LoginResponse(token = token, user = user.toResponse()))
    }
}
