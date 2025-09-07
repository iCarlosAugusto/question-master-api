package com.questionmaster.api.security

import com.questionmaster.api.domain.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import java.util.*

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val userId = try {
            UUID.fromString(username)
        } catch (e: IllegalArgumentException) {
            throw UsernameNotFoundException("Invalid user ID format: $username")
        }

        val user = userRepository.findById(userId)
            .orElseThrow { UsernameNotFoundException("User not found with id: $userId") }

        return CustomUserDetails.create(user)
    }
}
