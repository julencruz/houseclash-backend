package com.houseclash.backend.infrastructure.config

import com.houseclash.backend.domain.port.*
import com.houseclash.backend.domain.usecase.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BeanConfig {

    // ---- User ----

    @Bean
    fun registerUserUsecase(userRepository: UserRepository, passwordEncoder: PasswordEncoder) =
        RegisterUserUsecase(userRepository, passwordEncoder)

    @Bean
    fun loginUserUsecase(userRepository: UserRepository, passwordEncoder: PasswordEncoder) =
        LoginUserUsecase(userRepository, passwordEncoder)

    @Bean
    fun getUserProfileUsecase(userRepository: UserRepository) =
        GetUserProfileUsecase(userRepository)

    @Bean
    fun updateUserUsecase(userRepository: UserRepository, passwordEncoder: PasswordEncoder) =
        UpdateUserUsecase(userRepository, passwordEncoder)

    // ---- House ----

    @Bean
    fun createHouseUsecase(userRepository: UserRepository, houseRepository: HouseRepository) =
        CreateHouseUsecase(userRepository, houseRepository)

    @Bean
    fun getHouseDetailsUsecase(userRepository: UserRepository, houseRepository: HouseRepository) =
        GetHouseDetailsUsecase(userRepository, houseRepository)

    @Bean
    fun joinHouseUsecase(userRepository: UserRepository, houseRepository: HouseRepository) =
        JoinHouseUsecase(userRepository, houseRepository)

    @Bean
    fun leaveHouseUsecase(
        userRepository: UserRepository,
        taskRepository: TaskRepository,
        cardRepository: CardRepository,
        houseRepository: HouseRepository,
        categoryRepository: CategoryRepository
    ) = LeaveHouseUsecase(userRepository, taskRepository, cardRepository, houseRepository, categoryRepository)

    @Bean
    fun kickMemberUsecase(
        userRepository: UserRepository,
        houseRepository: HouseRepository,
        taskRepository: TaskRepository,
        cardRepository: CardRepository
    ) = KickMemberUsecase(userRepository, houseRepository, taskRepository, cardRepository)

    @Bean
    fun transferHouseOwnershipUsecase(houseRepository: HouseRepository, userRepository: UserRepository) =
        TransferHouseOwnershipUsecase(houseRepository, userRepository)

    @Bean
    fun updateHouseUsecase(houseRepository: HouseRepository, userRepository: UserRepository) =
        UpdateHouseUsecase(houseRepository, userRepository)

    @Bean
    fun getHouseRankingUsecase(userRepository: UserRepository, taskRepository: TaskRepository) =
        GetHouseRankingUsecase(userRepository, taskRepository)

    // ---- Category ----

    @Bean
    fun getHouseCategoriesUsecase(userRepository: UserRepository, categoryRepository: CategoryRepository) =
        GetHouseCategoriesUsecase(userRepository, categoryRepository)

    @Bean
    fun createCategoryHouseUsecase(
        houseRepository: HouseRepository,
        categoryRepository: CategoryRepository,
        userRepository: UserRepository
    ) = CreateCategoryHouseUsecase(houseRepository, categoryRepository, userRepository)

    @Bean
    fun updateCategoryUsecase(
        categoryRepository: CategoryRepository,
        userRepository: UserRepository,
        houseRepository: HouseRepository
    ) = UpdateCategoryUsecase(categoryRepository, userRepository, houseRepository)

    @Bean
    fun deleteCategoryUsecase(
        categoryRepository: CategoryRepository,
        userRepository: UserRepository,
        taskRepository: TaskRepository,
        houseRepository: HouseRepository
    ) = DeleteCategoryUsecase(categoryRepository, userRepository, taskRepository, houseRepository)

    // ---- Task ----

    @Bean
    fun getActiveTasksUsecase(userRepository: UserRepository, taskRepository: TaskRepository) =
        GetActiveTasksUsecase(userRepository, taskRepository)

    @Bean
    fun createTaskUsecase(
        taskRepository: TaskRepository,
        houseRepository: HouseRepository,
        categoryRepository: CategoryRepository,
        userRepository: UserRepository
    ) = CreateTaskUsecase(taskRepository, houseRepository, categoryRepository, userRepository)

    @Bean
    fun updateTaskUsecase(
        taskRepository: TaskRepository,
        userRepository: UserRepository,
        categoryRepository: CategoryRepository
    ) = UpdateTaskUsecase(taskRepository, userRepository, categoryRepository)

    @Bean
    fun deleteTaskUsecase(taskRepository: TaskRepository, houseRepository: HouseRepository) =
        DeleteTaskUsecase(taskRepository, houseRepository)

    @Bean
    fun assignTaskUsecase(userRepository: UserRepository, taskRepository: TaskRepository) =
        AssignTaskUsecase(userRepository, taskRepository)

    @Bean
    fun unassignTaskUsecase(taskRepository: TaskRepository, userRepository: UserRepository) =
        UnassignTaskUsecase(taskRepository, userRepository)

    @Bean
    fun completeTaskUsecase(taskRepository: TaskRepository) =
        CompleteTaskUsecase(taskRepository)

    @Bean
    fun validateTaskUsecase(taskRepository: TaskRepository, userRepository: UserRepository) =
        ValidateTaskUsecase(taskRepository, userRepository)

    // ---- Card ----

    @Bean
    fun getUserCardsUsecase(userRepository: UserRepository, cardRepository: CardRepository) =
        GetUserCardsUsecase(userRepository, cardRepository)

    @Bean
    fun openCardPackUsecase(userRepository: UserRepository, cardRepository: CardRepository) =
        OpenCardPackUsecase(userRepository, cardRepository)

    @Bean
    fun executeCardEffectUsecase(
        cardRepository: CardRepository,
        userRepository: UserRepository,
        taskRepository: TaskRepository
    ) = ExecuteCardEffectUsecase(cardRepository, userRepository, taskRepository)

    // ---- Scheduler ----

    @Bean
    fun applyMarketInflationUsecase(taskRepository: TaskRepository) =
        ApplyMarketInflationUsecase(taskRepository)

    @Bean
    fun autoApproveExpiredTasksUsecase(taskRepository: TaskRepository, userRepository: UserRepository) =
        AutoApproveExpiredTasksUsecase(taskRepository, userRepository)

    @Bean
    fun recurringTaskSchedulerUsecase(taskRepository: TaskRepository) =
        RecurringTaskSchedulerUsecase(taskRepository)
}
