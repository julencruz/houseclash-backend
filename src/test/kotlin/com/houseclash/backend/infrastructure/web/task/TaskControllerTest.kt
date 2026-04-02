package com.houseclash.backend.infrastructure.web.task

import com.houseclash.backend.domain.model.Category
import com.houseclash.backend.domain.model.Effort
import com.houseclash.backend.domain.model.House
import com.houseclash.backend.domain.model.TaskStatus
import com.houseclash.backend.domain.model.User
import com.houseclash.backend.domain.usecase.*
import com.houseclash.backend.helper.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

class TaskControllerTest {

    private val userRepository = UserRepositoryTester()
    private val houseRepository = HouseRepositoryTester()
    private val taskRepository = TaskRepositoryTester()
    private val categoryRepository = CategoryRepositoryTester()
    private val passwordEncoder = PasswordEncoderTester()

    private val registerUserUsecase = RegisterUserUsecase(userRepository, passwordEncoder)
    private val createHouseUsecase = CreateHouseUsecase(userRepository, houseRepository)
    private val joinHouseUsecase = JoinHouseUsecase(userRepository, houseRepository)
    private val getActiveTasksUsecase = GetActiveTasksUsecase(userRepository, taskRepository)
    private val createTaskUsecase = CreateTaskUsecase(taskRepository, houseRepository, categoryRepository, userRepository)
    private val updateTaskUsecase = UpdateTaskUsecase(taskRepository, userRepository, categoryRepository)
    private val deleteTaskUsecase = DeleteTaskUsecase(taskRepository, houseRepository)
    private val assignTaskUsecase = AssignTaskUsecase(userRepository, taskRepository)
    private val unassignTaskUsecase = UnassignTaskUsecase(taskRepository, userRepository)
    private val completeTaskUsecase = CompleteTaskUsecase(taskRepository)
    private val validateTaskUsecase = ValidateTaskUsecase(taskRepository, userRepository)

    private val controller = TaskController(
        getActiveTasksUsecase,
        createTaskUsecase,
        updateTaskUsecase,
        deleteTaskUsecase,
        assignTaskUsecase,
        unassignTaskUsecase,
        completeTaskUsecase,
        validateTaskUsecase
    )

    private lateinit var captain: User
    private lateinit var member: User
    private lateinit var house: House
    private lateinit var category: Category

    @BeforeEach
    fun setUp() {
        captain = registerUserUsecase.execute("Captain", "captain@email.com", "Password1")
        house = createHouseUsecase.execute(captain.id!!, "Test House")
        member = registerUserUsecase.execute("Member", "member@email.com", "Password1")
        joinHouseUsecase.execute(member.id!!, house.inviteCode)
        category = categoryRepository.save(Category.create(house.id!!, "Cleaning"))
        // Refresh captain from repository (houseId is updated after createHouseUsecase)
        captain = userRepository.findById(captain.id!!)!!
        member = userRepository.findById(member.id!!)!!
    }

    private fun authAs(user: User) = UsernamePasswordAuthenticationToken(user.id!!, null, emptyList())

    // ---- getActiveTasks ----

    @Test
    fun `should return 200 with empty list when house has no tasks`() {
        val response = controller.getActiveTasks(authAs(captain))

        assertEquals(HttpStatus.OK, response.statusCode)
        assertTrue(response.body!!.isEmpty())
    }

    @Test
    fun `should return 200 with active tasks for the house`() {
        createTaskUsecase.execute(captain.id!!, "Sweep floor", null, Effort.LOW, null, house.id!!, category.id!!)
        createTaskUsecase.execute(captain.id!!, "Mop floor", null, Effort.MEDIUM, null, house.id!!, category.id!!)

        val response = controller.getActiveTasks(authAs(captain))

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(2, response.body?.size)
    }

    @Test
    fun `should throw when user does not belong to a house`() {
        val outsider = registerUserUsecase.execute("Outsider", "outsider@email.com", "Password1")

        assertThrows(IllegalArgumentException::class.java) {
            controller.getActiveTasks(authAs(outsider))
        }
    }

    // ---- create ----

    @Test
    fun `should return 201 with created task`() {
        val request = CreateTaskRequest(
            title = "Clean kitchen",
            effort = Effort.MEDIUM,
            houseId = house.id!!,
            categoryId = category.id!!
        )

        val response = controller.create(request, authAs(captain))

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals("Clean kitchen", response.body?.title)
        assertEquals(TaskStatus.OPEN, response.body?.status)
        assertEquals(Effort.MEDIUM, response.body?.effort)
    }

    @Test
    fun `should return 201 with correct kudos value based on effort`() {
        val response = controller.create(
            CreateTaskRequest(title = "Task", effort = Effort.HIGH, houseId = house.id!!, categoryId = category.id!!),
            authAs(captain)
        )

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals(Effort.HIGH.baseKudos, response.body?.kudosValue)
    }

    @Test
    fun `should throw when creating task with non-existent category`() {
        assertThrows(IllegalArgumentException::class.java) {
            controller.create(
                CreateTaskRequest(title = "Task", effort = Effort.LOW, houseId = house.id!!, categoryId = 999L),
                authAs(captain)
            )
        }
    }

    @Test
    fun `should throw when creating task with blank title`() {
        assertThrows(IllegalArgumentException::class.java) {
            controller.create(
                CreateTaskRequest(title = "", effort = Effort.LOW, houseId = house.id!!, categoryId = category.id!!),
                authAs(captain)
            )
        }
    }

    // ---- update ----

    @Test
    fun `should return 200 with updated task title`() {
        val task = createTaskUsecase.execute(captain.id!!, "Old Title", null, Effort.LOW, null, house.id!!, category.id!!)

        val response = controller.update(task.id!!, UpdateTaskRequest(title = "New Title"), authAs(captain))

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("New Title", response.body?.title)
    }

    @Test
    fun `should return 200 with updated effort and kudos value`() {
        val task = createTaskUsecase.execute(captain.id!!, "Task", null, Effort.LOW, null, house.id!!, category.id!!)

        val response = controller.update(task.id!!, UpdateTaskRequest(effort = Effort.HIGH), authAs(captain))

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(Effort.HIGH, response.body?.effort)
    }

    @Test
    fun `should throw when updating a non-existent task`() {
        assertThrows(IllegalArgumentException::class.java) {
            controller.update(999L, UpdateTaskRequest(title = "X"), authAs(captain))
        }
    }

    // ---- delete ----

    @Test
    fun `should return 204 when captain deletes an OPEN task`() {
        val task = createTaskUsecase.execute(captain.id!!, "Task to delete", null, Effort.LOW, null, house.id!!, category.id!!)

        val response = controller.delete(task.id!!, authAs(captain))

        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
    }

    @Test
    fun `should throw when non-captain tries to delete a task`() {
        val task = createTaskUsecase.execute(captain.id!!, "Task", null, Effort.LOW, null, house.id!!, category.id!!)

        assertThrows(IllegalArgumentException::class.java) {
            controller.delete(task.id!!, authAs(member))
        }
    }

    @Test
    fun `should throw when deleting a non-existent task`() {
        assertThrows(IllegalArgumentException::class.java) {
            controller.delete(999L, authAs(captain))
        }
    }

    // ---- assign ----

    @Test
    fun `should return 200 with task in ASSIGNED status`() {
        val task = createTaskUsecase.execute(captain.id!!, "Task", null, Effort.LOW, null, house.id!!, category.id!!)

        val response = controller.assign(task.id!!, authAs(member))

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(TaskStatus.ASSIGNED, response.body?.status)
        assertEquals(member.id, response.body?.assignedTo)
    }

    @Test
    fun `should throw when assigning an already assigned task`() {
        val task = createTaskUsecase.execute(captain.id!!, "Task", null, Effort.LOW, null, house.id!!, category.id!!)
        assignTaskUsecase.execute(task.id!!, captain.id!!)

        assertThrows(IllegalArgumentException::class.java) {
            controller.assign(task.id, authAs(member))
        }
    }

    // ---- unassign ----

    @Test
    fun `should return 200 with task back to OPEN after unassign`() {
        val task = createTaskUsecase.execute(captain.id!!, "Task", null, Effort.LOW, null, house.id!!, category.id!!)
        assignTaskUsecase.execute(task.id!!, member.id!!)

        val response = controller.unassign(task.id, authAs(member))

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(TaskStatus.OPEN, response.body?.status)
        assertNull(response.body?.assignedTo)
    }

    @Test
    fun `should throw when unassigning a task belonging to another user`() {
        val task = createTaskUsecase.execute(captain.id!!, "Task", null, Effort.LOW, null, house.id!!, category.id!!)
        assignTaskUsecase.execute(task.id!!, member.id!!)

        assertThrows(IllegalArgumentException::class.java) {
            controller.unassign(task.id, authAs(captain))
        }
    }

    // ---- complete ----

    @Test
    fun `should return 200 with task in PENDING_REVIEW status`() {
        val task = createTaskUsecase.execute(captain.id!!, "Task", null, Effort.LOW, null, house.id!!, category.id!!)
        assignTaskUsecase.execute(task.id!!, member.id!!)

        val response = controller.complete(task.id)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(TaskStatus.PENDING_REVIEW, response.body?.status)
        assertNotNull(response.body?.completedAt)
    }

    @Test
    fun `should throw when completing a task that is not assigned`() {
        val task = createTaskUsecase.execute(captain.id!!, "Task", null, Effort.LOW, null, house.id!!, category.id!!)

        assertThrows(IllegalArgumentException::class.java) {
            controller.complete(task.id!!)
        }
    }

    // ---- validate ----

    @Test
    fun `should return 200 with APPROVED status on APPROVE decision`() {
        val task = createTaskUsecase.execute(captain.id!!, "Task", null, Effort.LOW, null, house.id!!, category.id!!)
        assignTaskUsecase.execute(task.id!!, member.id!!)
        completeTaskUsecase.execute(task.id)

        val response = controller.validate(task.id, ValidateTaskRequest("APPROVE"), authAs(captain))

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(TaskStatus.APPROVED, response.body?.status)
    }

    @Test
    fun `should return 200 with DISPUTED status on DISPUTE decision`() {
        val task = createTaskUsecase.execute(captain.id!!, "Task", null, Effort.LOW, null, house.id!!, category.id!!)
        assignTaskUsecase.execute(task.id!!, member.id!!)
        completeTaskUsecase.execute(task.id)

        val response = controller.validate(task.id, ValidateTaskRequest("DISPUTE"), authAs(captain))

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(TaskStatus.DISPUTED, response.body?.status)
    }

    @Test
    fun `should throw when user validates their own task`() {
        val task = createTaskUsecase.execute(captain.id!!, "Task", null, Effort.LOW, null, house.id!!, category.id!!)
        assignTaskUsecase.execute(task.id!!, member.id!!)
        completeTaskUsecase.execute(task.id)

        assertThrows(IllegalArgumentException::class.java) {
            controller.validate(task.id, ValidateTaskRequest("APPROVE"), authAs(member))
        }
    }

    @Test
    fun `should throw when validating a task that is not PENDING_REVIEW`() {
        val task = createTaskUsecase.execute(captain.id!!, "Task", null, Effort.LOW, null, house.id!!, category.id!!)
        assignTaskUsecase.execute(task.id!!, member.id!!)

        assertThrows(IllegalArgumentException::class.java) {
            controller.validate(task.id, ValidateTaskRequest("APPROVE"), authAs(captain))
        }
    }
}
