package com.houseclash.backend.infrastructure.web.user

import com.houseclash.backend.domain.usecase.GetUserProfileUsecase
import com.houseclash.backend.domain.usecase.LoginUserUsecase
import com.houseclash.backend.domain.usecase.RegisterUserUsecase
import com.houseclash.backend.domain.usecase.UpdateUserUsecase
import com.houseclash.backend.helper.PasswordEncoderTester
import com.houseclash.backend.helper.UserRepositoryTester
import com.houseclash.backend.infrastructure.security.JwtService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

class UserControllerTest {

    private val userRepository = UserRepositoryTester()
    private val passwordEncoder = PasswordEncoderTester()
    private val jwtService = JwtService("houseclash-test-secret-key-for-unit-tests-only")

    private val registerUserUsecase = RegisterUserUsecase(userRepository, passwordEncoder)
    private val loginUserUsecase = LoginUserUsecase(userRepository, passwordEncoder)
    private val getUserProfileUsecase = GetUserProfileUsecase(userRepository)
    private val updateUserUsecase = UpdateUserUsecase(userRepository, passwordEncoder)

    private val controller = UserController(
        registerUserUsecase,
        loginUserUsecase,
        getUserProfileUsecase,
        updateUserUsecase,
        jwtService
    )

    // ---- register ----

    @Test
    fun `should return 201 and user data when registering a new user`() {
        val response = controller.register(RegisterRequest("TestUser", "test@email.com", "Password1"))

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals("TestUser", response.body?.username)
        assertEquals("test@email.com", response.body?.email)
        assertNotNull(response.body?.id)
    }

    @Test
    fun `should throw when registering with a duplicate email`() {
        controller.register(RegisterRequest("TestUser", "test@email.com", "Password1"))

        assertThrows(IllegalArgumentException::class.java) {
            controller.register(RegisterRequest("OtherUser", "test@email.com", "Password2"))
        }
    }

    // ---- login ----

    @Test
    fun `should return 200 with token and user on successful login`() {
        controller.register(RegisterRequest("TestUser", "test@email.com", "Password1"))

        val response = controller.login(LoginRequest("test@email.com", "Password1"))

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body?.token)
        assertEquals("TestUser", response.body?.user?.username)
    }

    @Test
    fun `should return a valid JWT token on login`() {
        controller.register(RegisterRequest("TestUser", "test@email.com", "Password1"))

        val response = controller.login(LoginRequest("test@email.com", "Password1"))

        val token = response.body!!.token
        assertTrue(jwtService.isTokenValid(token))
    }

    @Test
    fun `should throw when login with wrong password`() {
        controller.register(RegisterRequest("TestUser", "test@email.com", "Password1"))

        assertThrows(IllegalArgumentException::class.java) {
            controller.login(LoginRequest("test@email.com", "WrongPassword"))
        }
    }

    @Test
    fun `should throw when login with non-existent email`() {
        assertThrows(IllegalArgumentException::class.java) {
            controller.login(LoginRequest("noone@email.com", "Password1"))
        }
    }

    // ---- getProfile ----

    @Test
    fun `should return 200 with user profile`() {
        val user = registerUserUsecase.execute("TestUser", "test@email.com", "Password1")
        val auth = UsernamePasswordAuthenticationToken(user.id!!, null, emptyList())

        val response = controller.getProfile(auth)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(user.id, response.body?.id)
        assertEquals("TestUser", response.body?.username)
    }

    @Test
    fun `should throw when getting profile of non-existent user`() {
        val auth = UsernamePasswordAuthenticationToken(999L, null, emptyList())

        assertThrows(IllegalArgumentException::class.java) {
            controller.getProfile(auth)
        }
    }

    // ---- updateProfile ----

    @Test
    fun `should return 200 and updated username`() {
        val user = registerUserUsecase.execute("TestUser", "test@email.com", "Password1")
        val auth = UsernamePasswordAuthenticationToken(user.id!!, null, emptyList())

        val response = controller.updateProfile(UpdateUserRequest(username = "NewName"), auth)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("NewName", response.body?.username)
    }

    @Test
    fun `should return 200 and keep old username when not provided`() {
        val user = registerUserUsecase.execute("TestUser", "test@email.com", "Password1")
        val auth = UsernamePasswordAuthenticationToken(user.id!!, null, emptyList())

        val response = controller.updateProfile(UpdateUserRequest(), auth)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("TestUser", response.body?.username)
    }

    @Test
    fun `should return 200 when updating password with correct old password`() {
        val user = registerUserUsecase.execute("TestUser", "test@email.com", "Password1")
        val auth = UsernamePasswordAuthenticationToken(user.id!!, null, emptyList())

        val response = controller.updateProfile(
            UpdateUserRequest(oldPassword = "Password1", newPassword = "Password2"),
            auth
        )

        assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    fun `should throw when updating password with incorrect old password`() {
        val user = registerUserUsecase.execute("TestUser", "test@email.com", "Password1")
        val auth = UsernamePasswordAuthenticationToken(user.id!!, null, emptyList())

        assertThrows(IllegalArgumentException::class.java) {
            controller.updateProfile(
                UpdateUserRequest(oldPassword = "WrongOld", newPassword = "Password2"),
                auth
            )
        }
    }
}
