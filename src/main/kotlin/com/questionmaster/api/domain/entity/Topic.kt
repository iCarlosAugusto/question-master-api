package com.questionmaster.api.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "topics",
    uniqueConstraints = [UniqueConstraint(columnNames = ["subject_id", "name"])]
)
class Topic(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    val subject: Subject,
    
    @Column(nullable = false)
    val name: String,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @ManyToMany(mappedBy = "topics", fetch = FetchType.LAZY)
    val questions: MutableSet<Question> = mutableSetOf()
) {
    // JPA requires a no-arg constructor
    constructor() : this(
        subject = Subject(),
        name = "",
        createdAt = LocalDateTime.now()
    )

    fun copy(
        id: Long = this.id,
        subject: Subject = this.subject,
        name: String = this.name,
        createdAt: LocalDateTime = this.createdAt,
        questions: MutableSet<Question> = this.questions
    ): Topic {
        return Topic(
            id = id,
            subject = subject,
            name = name,
            createdAt = createdAt,
            questions = questions
        )
    }
}
