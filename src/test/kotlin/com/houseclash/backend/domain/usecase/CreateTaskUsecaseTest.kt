package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.Effort
import com.houseclash.backend.helper.CategoryRepositoryTester
import com.houseclash.backend.helper.HouseRepositoryTester
import com.houseclash.backend.helper.PasswordEncoderTester
import com.houseclash.backend.helper.TaskRepositoryTester
import com.houseclash.backend.helper.TestDataFactory
import com.houseclash.backend.helper.UserRepositoryTester
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class CreateTaskUsecaseTest {
    private val userRepository = UserRepositoryTester()
    private val houseRepository = HouseRepositoryTester()
    private val categoryRepository = CategoryRepositoryTester()
    private val registerUsecase = RegisterUserUsecase(userRepository, PasswordEncoderTester())
    private val taskRepository = TaskRepositoryTester()
    private val createHouseUsecase = CreateHouseUsecase(userRepository, houseRepository)
    private val usecase = CreateTaskUsecase(taskRepository, houseRepository, categoryRepository, userRepository)

    @Test
    fun `sould create task succesfully`() {
        val user = registerUsecase.execute("Test", "test@email.com", "Password1")
        val house = createHouseUsecase.execute(user.id!!, "Pis de Gràcia")
        val category = TestDataFactory.createCategory(categoryRepository, house.id!!)
        val task = usecase.execute(user.id, "Comprar pa", "Comprar pa al forn de la cantonada", Effort.MEDIUM, null, house.id, category.id!!)
        assertEquals("Comprar pa", task.title)
    }

    @Test
    fun `should initialize kudos value from effort`() {
        val user = registerUsecase.execute("Test", "test@email.com", "Password1")
        val house = createHouseUsecase.execute(user.id!!, "Pis de Gràcia")
        val category = TestDataFactory.createCategory(categoryRepository, house.id!!)
        val task = usecase.execute(user.id, "Comprar pa", null, Effort.MEDIUM, null, house.id, category.id!!)
        assertEquals(4, task.kudosValue)
    }

    @Test
    fun `should throw when house does not exist`() {
        val user = registerUsecase.execute("Test", "test@email.com", "Password1")
        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(user.id!!, "Comprar pa", null, Effort.LOW, null, 999L, 1L)
        }
    }

    @Test
    fun `should throw when title is blank`() {
        val user = registerUsecase.execute("Test", "test@email.com", "Password1")
        val house = createHouseUsecase.execute(user.id!!, "Pis de Gràcia")
        val category = TestDataFactory.createCategory(categoryRepository, house.id!!)
        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(user.id, "", null, Effort.LOW, null, house.id, category.id!!)
        }
    }

    @Test
    fun `should throw when category does not exist`() {
        val user = registerUsecase.execute("Test", "test@email.com", "Password1")
        val house = createHouseUsecase.execute(user.id!!, "Pis de Gràcia")
        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(user.id, "Comprar pa", null, Effort.LOW, null, house.id!!, 999L)
        }
    }

    @Test
    fun `should throw when category belongs to a different house`() {
        val user = registerUsecase.execute("Rich", "rich@email.com", "Password1")
        val user2 = registerUsecase.execute("Poor", "poor@email.com", "Password1")
        val house1 = createHouseUsecase.execute(user.id!!, "Pis de Ric")
        val house2 = createHouseUsecase.execute(user2.id!!, "Pis de Pobre")
        val categoryFromHouse2 = TestDataFactory.createCategory(categoryRepository, house2.id!!)
        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(user.id, "Comprar pa", null, Effort.LOW, null, house1.id!!, categoryFromHouse2.id!!)
        }
    }
}
