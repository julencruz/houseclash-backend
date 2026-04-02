package com.houseclash.backend.domain.usecase

import com.houseclash.backend.helper.HouseRepositoryTester
import com.houseclash.backend.helper.TestDataFactory
import com.houseclash.backend.helper.UserRepositoryTester
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TransferHouseOwnershipUsecaseTest {
    private val houseRepository = HouseRepositoryTester()
    private val userRepository = UserRepositoryTester()
    private val usecase = TransferHouseOwnershipUsecase(houseRepository, userRepository)

    @Test
    fun `should successfully transfer ownership to another member`() {
        val captain = TestDataFactory.createUser(userRepository, username = "Captain", email = "c@m.com")
        val house = TestDataFactory.createHouse(houseRepository, userRepository, captain)
        require(house.id != null)
        require(captain.id != null)

        val newOwner = TestDataFactory.createUser(userRepository, username = "Heir", email = "h@m.com")
        userRepository.save(newOwner.joinHouse(house.id))
        require(newOwner.id != null)

        val result = usecase.execute(currentOwnerId = captain.id, newOwnerId = newOwner.id)

        assertEquals(newOwner.id, result.createdBy)
    }

    @Test
    fun `should fail if a regular member tries to transfer ownership`() {
        val captain = TestDataFactory.createUser(userRepository, username = "Captain", email = "c@m.com")
        val house = TestDataFactory.createHouse(houseRepository, userRepository, captain)
        require(house.id != null)

        val usurper = TestDataFactory.createUser(userRepository, username = "Usurper", email = "u@m.com")
        userRepository.save(usurper.joinHouse(house.id))
        require(usurper.id != null)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(currentOwnerId = usurper.id, newOwnerId = usurper.id)
        }
        assertEquals("Only the current captain can transfer ownership.", exception.message)
    }

    @Test
    fun `should fail if new owner belongs to another house`() {
        val captain = TestDataFactory.createUser(userRepository, username = "Captain", email = "c@m.com")
        TestDataFactory.createHouse(houseRepository, userRepository, captain)
        require(captain.id != null)

        val outsider = TestDataFactory.createUser(userRepository, username = "Outsider", email = "o@m.com")
        TestDataFactory.createHouse(houseRepository, userRepository, outsider)
        require(outsider.id != null)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            usecase.execute(currentOwnerId = captain.id, newOwnerId = outsider.id)
        }
        assertEquals("The new captain must belong to the same house.", exception.message)
    }
}
