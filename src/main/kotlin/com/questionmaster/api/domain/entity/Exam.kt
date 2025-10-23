package com.questionmaster.api.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "exams", schema = "public")
class Exam(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(nullable = false, length = 200)
    val name: String,

    @Column(length = 20)
    val slug: String,
    
    @Column(length = 100)
    val institution: String? = null,
    
    @Column(columnDefinition = "TEXT")
    val description: String? = null,
    
    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @OneToMany(mappedBy = "exam", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val questions: MutableSet<Question> = mutableSetOf()
) {
    // JPA requires a no-arg constructor
    constructor() : this(
        name = "",
        slug = ""
    )

    fun copy(
        id: Long = this.id,
        name: String = this.name,
        institution: String? = this.institution,
        description: String? = this.description,
        isActive: Boolean = this.isActive,
        createdAt: LocalDateTime = this.createdAt,
        updatedAt: LocalDateTime = this.updatedAt,
        questions: MutableSet<Question> = this.questions
    ): Exam {
        return Exam(
            id = id,
            name = name,
            slug = slug,
            institution = institution,
            description = description,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt,
            questions = questions
        )
    }
}

