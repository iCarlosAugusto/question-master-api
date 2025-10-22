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

    fun createUser(
        id: UUID, 
        displayName: String? = null, 
        role: AppRole = AppRole.USER,
        email: String? = null
    ): User {
        val user = User(
            id = id,
            displayName = displayName,
            role = role,
            email = email
        )
        return userRepository.save(user)
    }
    
    /**
     * Gets or creates a user by ID. 
     * This is useful for Supabase integration where the user might already exist in auth.users
     * but not yet in our profiles table (before trigger execution).
     */
    fun getOrCreateUser(
        id: UUID,
        email: String,
        displayName: String? = null,
        role: AppRole = AppRole.USER
    ): User {
        return userRepository.findById(id).orElseGet {
            createUser(
                id = id,
                email = email,
                displayName = displayName ?: email.substringBefore("@"),
                role = role
            )
        }
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
    
    @Transactional(readOnly = true)
    fun getUserByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }
    
    @Transactional(readOnly = true)
    fun existsByEmail(email: String): Boolean {
        return userRepository.existsByEmail(email)
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
