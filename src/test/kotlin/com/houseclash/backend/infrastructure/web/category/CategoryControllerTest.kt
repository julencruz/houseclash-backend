package com.houseclash.backend.infrastructure.web.category

import com.houseclash.backend.domain.model.House
import com.houseclash.backend.domain.model.User
import com.houseclash.backend.domain.usecase.*
import com.houseclash.backend.helper.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

class CategoryControllerTest {

    private val userRepository = UserRepositoryTester()
    private val houseRepository = HouseRepositoryTester()
    private val taskRepository = TaskRepositoryTester()
    private val categoryRepository = CategoryRepositoryTester()
    private val passwordEncoder = PasswordEncoderTester()
    private val activityLogRepository = ActivityLogRepositoryTester()

    private val registerUserUsecase = RegisterUserUsecase(userRepository, passwordEncoder)
    private val createHouseUsecase = CreateHouseUsecase(userRepository, houseRepository)
    private val joinHouseUsecase = JoinHouseUsecase(userRepository, houseRepository, activityLogRepository)
    private val getHouseCategoriesUsecase = GetHouseCategoriesUsecase(userRepository, categoryRepository)
    private val createCategoryHouseUsecase = CreateCategoryHouseUsecase(houseRepository, categoryRepository, userRepository)
    private val updateCategoryUsecase = UpdateCategoryUsecase(categoryRepository, userRepository, houseRepository)
    private val deleteCategoryUsecase = DeleteCategoryUsecase(categoryRepository, userRepository, taskRepository, houseRepository)

    private val controller = CategoryController(
        getHouseCategoriesUsecase,
        createCategoryHouseUsecase,
        updateCategoryUsecase,
        deleteCategoryUsecase
    )

    private lateinit var captain: User
    private lateinit var house: House

    @BeforeEach
    fun setUp() {
        captain = registerUserUsecase.execute("Captain", "captain@email.com", "Password1")
        house = createHouseUsecase.execute(captain.id!!, "Test House")
    }

    private fun captainAuth() = UsernamePasswordAuthenticationToken(captain.id!!, null, emptyList())

    // ---- getCategories ----

    @Test
    fun `should return 200 with empty list when house has no categories`() {
        val response = controller.getCategories(captainAuth())

        assertEquals(HttpStatus.OK, response.statusCode)
        assertTrue(response.body!!.isEmpty())
    }

    @Test
    fun `should return 200 with all categories belonging to the house`() {
        createCategoryHouseUsecase.execute(captain.id!!, house.id!!, "Kitchen")
        createCategoryHouseUsecase.execute(captain.id!!, house.id!!, "Bathroom")

        val response = controller.getCategories(captainAuth())

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(2, response.body?.size)
    }

    @Test
    fun `should throw when user does not belong to any house`() {
        val outsider = registerUserUsecase.execute("Outsider", "outsider@email.com", "Password1")
        val auth = UsernamePasswordAuthenticationToken(outsider.id!!, null, emptyList())

        assertThrows(IllegalArgumentException::class.java) {
            controller.getCategories(auth)
        }
    }

    // ---- create ----

    @Test
    fun `should return 201 with category data when captain creates a category`() {
        val response = controller.create(
            CreateCategoryRequest(name = "Kitchen", houseId = house.id!!),
            captainAuth()
        )

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals("Kitchen", response.body?.name)
        assertEquals(house.id, response.body?.houseId)
    }

    @Test
    fun `should return 201 with description when provided`() {
        val response = controller.create(
            CreateCategoryRequest(name = "Kitchen", description = "Shared kitchen tasks", houseId = house.id!!),
            captainAuth()
        )

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals("Shared kitchen tasks", response.body?.description)
    }

    @Test
    fun `should throw when a non-captain member tries to create a category`() {
        val member = registerUserUsecase.execute("Member", "member@email.com", "Password1")
        joinHouseUsecase.execute(member.id!!, house.inviteCode)
        val auth = UsernamePasswordAuthenticationToken(member.id, null, emptyList())

        assertThrows(IllegalArgumentException::class.java) {
            controller.create(CreateCategoryRequest(name = "Kitchen", houseId = house.id!!), auth)
        }
    }

    @Test
    fun `should throw when creating a category with a duplicate name`() {
        createCategoryHouseUsecase.execute(captain.id!!, house.id!!, "Kitchen")

        assertThrows(IllegalArgumentException::class.java) {
            controller.create(CreateCategoryRequest(name = "Kitchen", houseId = house.id!!), captainAuth())
        }
    }

    // ---- update ----

    @Test
    fun `should return 200 with updated category name`() {
        val category = createCategoryHouseUsecase.execute(captain.id!!, house.id!!, "Kichen")

        val response = controller.update(category.id!!, UpdateCategoryRequest("Kitchen"), captainAuth())

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("Kitchen", response.body?.name)
    }

    @Test
    fun `should throw when a non-captain tries to update a category`() {
        val category = createCategoryHouseUsecase.execute(captain.id!!, house.id!!, "Kitchen")
        val member = registerUserUsecase.execute("Member", "member@email.com", "Password1")
        joinHouseUsecase.execute(member.id!!, house.inviteCode)
        val auth = UsernamePasswordAuthenticationToken(member.id, null, emptyList())

        assertThrows(IllegalArgumentException::class.java) {
            controller.update(category.id!!, UpdateCategoryRequest("New Name"), auth)
        }
    }

    @Test
    fun `should throw when updating to an existing category name`() {
        createCategoryHouseUsecase.execute(captain.id!!, house.id!!, "Kitchen")
        val bathroom = createCategoryHouseUsecase.execute(captain.id!!, house.id!!, "Bathroom")

        assertThrows(IllegalArgumentException::class.java) {
            controller.update(bathroom.id!!, UpdateCategoryRequest("Kitchen"), captainAuth())
        }
    }

    // ---- delete ----

    @Test
    fun `should return 204 when captain deletes an empty category`() {
        val category = createCategoryHouseUsecase.execute(captain.id!!, house.id!!, "Kitchen")

        val response = controller.delete(category.id!!, captainAuth())

        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
    }

    @Test
    fun `should throw when a non-captain tries to delete a category`() {
        val category = createCategoryHouseUsecase.execute(captain.id!!, house.id!!, "Kitchen")
        val member = registerUserUsecase.execute("Member", "member@email.com", "Password1")
        joinHouseUsecase.execute(member.id!!, house.inviteCode)
        val auth = UsernamePasswordAuthenticationToken(member.id, null, emptyList())

        assertThrows(IllegalArgumentException::class.java) {
            controller.delete(category.id!!, auth)
        }
    }

    @Test
    fun `should throw when deleting a category that has tasks`() {
        val category = createCategoryHouseUsecase.execute(captain.id!!, house.id!!, "Kitchen")
        taskRepository.save(
            com.houseclash.backend.domain.model.Task.create(
                title = "Clean stove",
                description = null,
                effort = com.houseclash.backend.domain.model.Effort.LOW,
                recurrence = null,
                houseId = house.id!!,
                categoryId = category.id!!
            )
        )

        assertThrows(IllegalArgumentException::class.java) {
            controller.delete(category.id, captainAuth())
        }
    }

    @Test
    fun `should throw when deleting a non-existent category`() {
        assertThrows(IllegalArgumentException::class.java) {
            controller.delete(999L, captainAuth())
        }
    }
}
