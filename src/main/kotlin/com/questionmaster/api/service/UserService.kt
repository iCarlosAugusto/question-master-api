package com.questionmaster.api.service

import com.questionmaster.api.domain.entity.User
import com.questionmaster.api.domain.enums.AppRole
import com.questionmaster.api.domain.repository.UserRepository
import com.questionmaster.api.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository
) {

    fun createUser(id: UUID, displayName: String? = null, role: AppRole = AppRole.USER): User {
        val user = User(
            id = id,
            displayName = displayName,
            role = role
        )
        return userRepository.save(user)
    }

    @Transactional(readOnly = true)
    fun getUserById(id: UUID): User {
        return userRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("User not found with id: $id") }
    }

    @Transactional(readOnly = true)
    fun getUserByDisplayName(displayName: String): User? {
        return userRepository.findByDisplayName(displayName)
    }

    fun updateUserRole(id: UUID, role: AppRole): User {
        val user = getUserById(id)
        val updatedUser = user.copy(role = role)
        return userRepository.save(updatedUser)
    }

    fun updateDisplayName(id: UUID, displayName: String): User {
        val user = getUserById(id)
        val updatedUser = user.copy(displayName = displayName)
        return userRepository.save(updatedUser)
    }

    @Transactional(readOnly = true)
    fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }

    fun deleteUser(id: UUID) {
        val user = getUserById(id)
        userRepository.delete(user)
    }
}
