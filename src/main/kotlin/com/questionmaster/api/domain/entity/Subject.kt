package com.questionmaster.api.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "subjects")
class Subject(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(nullable = false, unique = true)
    val name: String,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @OneToMany(mappedBy = "subject", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val topics: MutableSet<Topic> = mutableSetOf(),
    
    @OneToMany(mappedBy = "subject", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val questions: MutableSet<Question> = mutableSetOf(),
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id")
    val exam: Exam? = null
) {
    // JPA requires a no-arg constructor
    constructor() : this(
        name = "",
        createdAt = LocalDateTime.now()
    )

    fun copy(
        id: Long = this.id,
        name: String = this.name,
        createdAt: LocalDateTime = this.createdAt,
        topics: MutableSet<Topic> = this.topics,
        questions: MutableSet<Question> = this.questions,
        exam: Exam? = this.exam
    ): Subject {
        return Subject(
            id = id,
            name = name,
            createdAt = createdAt,
            topics = topics,
            questions = questions,
            exam = exam
        )
    }
}
