package com.houseclash.backend.domain.usecase

import com.houseclash.backend.domain.model.Effort
import com.houseclash.backend.helper.HouseRepositoryTester
import com.houseclash.backend.helper.PasswordEncoderTester
import com.houseclash.backend.helper.TaskRepositoryTester
import com.houseclash.backend.helper.UserRepositoryTester
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class CreateTaskUsecaseTest {
    private val userRepository = UserRepositoryTester()
    private val houseRepository = HouseRepositoryTester()
    private val registerUsecase = RegisterUserUsecase(userRepository, PasswordEncoderTester())
    private val taskRepository = TaskRepositoryTester()
    private val createHouseUsecase = CreateHouseUsecase(userRepository, houseRepository)
    private val usecase = CreateTaskUsecase(taskRepository, houseRepository)

    @Test
    fun `sould create task succesfully`() {
        val user = registerUsecase.execute("Test", "test@email.com", "Password1")
        val house = createHouseUsecase.execute(user.id!!, "Pis de Gràcia")
        val task = usecase.execute("Comprar pa", "Comprar pa al forn de la cantonada", Effort.MEDIUM, house.id!!)
        assertEquals("Comprar pa", task.title)
    }

    @Test
    fun `should initialize kudos value from effort`() {
        val user = registerUsecase.execute("Test", "test@email.com", "Password1")
        val house = createHouseUsecase.execute(user.id!!, "Pis de Gràcia")
        val task = usecase.execute("Comprar pa", null, Effort.MEDIUM, house.id!!)
        assertEquals(4, task.kudosValue)  // MEDIUM = 4 baseKudos
    }

    @Test
    fun `should throw when house does not exist`() {
        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute("Comprar pa", null, Effort.LOW, 999L)
        }
    }

    @Test
    fun `should throw when title is blank`() {
        val user = registerUsecase.execute("Test", "test@email.com", "Password1")
        val house = createHouseUsecase.execute(user.id!!, "Pis de Gràcia")
        assertThrows(IllegalArgumentException::class.java) {
            usecase.execute("", null, Effort.LOW, house.id!!)
        }
    }
}
