package com.questionmaster.api.domain.repository

import com.questionmaster.api.domain.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, UUID> {
    fun findByDisplayName(displayName: String): User?
    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean
}
