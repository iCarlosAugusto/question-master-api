package com.questionmaster.api.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "answers")
class Answer(
    @Id
    val id: UUID = UUID.randomUUID(),
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    val question: Question,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alternative_id", nullable = false)
    val alternative: Alternative,
    
    @Column(name = "is_correct", nullable = false)
    val isCorrect: Boolean,
    
    @Column(name = "answered_at", nullable = false)
    val answeredAt: LocalDateTime = LocalDateTime.now()
) {
    // JPA requires a no-arg constructor
    constructor() : this(
        user = User(),
        question = Question(),
        alternative = Alternative(),
        isCorrect = false
    )
}
