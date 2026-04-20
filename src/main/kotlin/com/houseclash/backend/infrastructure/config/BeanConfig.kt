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
    fun joinHouseUsecase(
        userRepository: UserRepository,
        houseRepository: HouseRepository,
        activityLogRepository: ActivityLogRepository
    ) = JoinHouseUsecase(userRepository, houseRepository, activityLogRepository)

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
        cardRepository: CardRepository,
        activityLogRepository: ActivityLogRepository
    ) = KickMemberUsecase(userRepository, houseRepository, taskRepository, cardRepository, activityLogRepository)

    @Bean
    fun transferHouseOwnershipUsecase(
        houseRepository: HouseRepository,
        userRepository: UserRepository,
        activityLogRepository: ActivityLogRepository
    ) = TransferHouseOwnershipUsecase(houseRepository, userRepository, activityLogRepository)

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
        userRepository: UserRepository,
        activityLogRepository: ActivityLogRepository
    ) = CreateTaskUsecase(taskRepository, houseRepository, categoryRepository, userRepository, activityLogRepository)

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
    fun assignTaskUsecase(
        userRepository: UserRepository,
        taskRepository: TaskRepository,
        activityLogRepository: ActivityLogRepository
    ) = AssignTaskUsecase(userRepository, taskRepository, activityLogRepository)

    @Bean
    fun unassignTaskUsecase(
        taskRepository: TaskRepository,
        userRepository: UserRepository,
        activityLogRepository: ActivityLogRepository
    ) = UnassignTaskUsecase(taskRepository, userRepository, activityLogRepository)

    @Bean
    fun completeTaskUsecase(
        taskRepository: TaskRepository,
        userRepository: UserRepository,
        activityLogRepository: ActivityLogRepository
    ) = CompleteTaskUsecase(taskRepository, userRepository, activityLogRepository)

    @Bean
    fun validateTaskUsecase(
        taskRepository: TaskRepository,
        userRepository: UserRepository,
        activityLogRepository: ActivityLogRepository
    ) = ValidateTaskUsecase(taskRepository, userRepository, activityLogRepository)

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
        taskRepository: TaskRepository,
        activityLogRepository: ActivityLogRepository
    ) = ExecuteCardEffectUsecase(cardRepository, userRepository, taskRepository, activityLogRepository)

    // ---- Scheduler ----

    @Bean
    fun applyMarketInflationUsecase(
        taskRepository: TaskRepository,
        activityLogRepository: ActivityLogRepository
    ) = ApplyMarketInflationUsecase(taskRepository, activityLogRepository)

    @Bean
    fun autoApproveExpiredTasksUsecase(
        taskRepository: TaskRepository,
        userRepository: UserRepository,
        activityLogRepository: ActivityLogRepository
    ) = AutoApproveExpiredTasksUsecase(taskRepository, userRepository, activityLogRepository)

    @Bean
    fun recurringTaskSchedulerUsecase(taskRepository: TaskRepository) =
        RecurringTaskSchedulerUsecase(taskRepository)

    // ---- Activity Log ----

    @Bean
    fun getActivityLogUsecase(
        activityLogRepository: ActivityLogRepository,
        taskRepository: TaskRepository
    ) = GetActivityLogUsecase(activityLogRepository, taskRepository)

    @Bean
    fun purgeActivityLogUsecase(
        activityLogRepository: ActivityLogRepository,
        taskRepository: TaskRepository
    ) = PurgeActivityLogUsecase(activityLogRepository, taskRepository)
}
