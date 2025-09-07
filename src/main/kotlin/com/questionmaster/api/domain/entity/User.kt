package com.questionmaster.api.domain.entity

import com.questionmaster.api.domain.enums.AppRole
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "profiles")
class User(
    @Id
    val id: UUID = UUID.randomUUID(),
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: AppRole = AppRole.USER,
    
    @Column(name = "display_name")
    val displayName: String? = null,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    // JPA requires a no-arg constructor
    constructor() : this(
        id = UUID.randomUUID(),
        role = AppRole.USER,
        displayName = null,
        createdAt = LocalDateTime.now()
    )

    fun copy(
        id: UUID = this.id,
        role: AppRole = this.role,
        displayName: String? = this.displayName,
        createdAt: LocalDateTime = this.createdAt
    ): User {
        return User(
            id = id,
            role = role,
            displayName = displayName,
            createdAt = createdAt
        )
    }
}
