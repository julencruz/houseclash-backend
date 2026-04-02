package com.houseclash.backend.infrastructure.config

import com.houseclash.backend.domain.port.UserRepository
import com.houseclash.backend.domain.port.PasswordEncoder
import com.houseclash.backend.domain.usecase.LoginUserUsecase
import com.houseclash.backend.domain.usecase.RegisterUserUsecase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BeanConfig {

    @Bean
    fun registerUserUsecase(
        userRepository: UserRepository,
        passwordEncoder: PasswordEncoder
    ): RegisterUserUsecase {
        return RegisterUserUsecase(userRepository, passwordEncoder)
    }

    @Bean
    fun loginUserUsecase(
        userRepository: UserRepository,
        passwordEncoder: PasswordEncoder
    ): LoginUserUsecase {
        return LoginUserUsecase(userRepository, passwordEncoder)
    }
}
