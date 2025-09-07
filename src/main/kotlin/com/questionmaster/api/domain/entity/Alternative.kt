package com.questionmaster.api.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(
    name = "alternatives",
    uniqueConstraints = [UniqueConstraint(columnNames = ["question_id", "ord"])]
)
class Alternative(
    @Id
    val id: UUID = UUID.randomUUID(),
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    val question: Question,
    
    @Column(nullable = false, columnDefinition = "TEXT")
    val body: String,
    
    @Column(name = "is_correct", nullable = false)
    val isCorrect: Boolean = false,
    
    @Column(nullable = false)
    val ord: Short,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @OneToMany(mappedBy = "alternative", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val answers: MutableSet<Answer> = mutableSetOf()
) {
    // JPA requires a no-arg constructor
    constructor() : this(
        question = Question(),
        body = "",
        ord = 1
    )
    
    // Computed property for label (A, B, C, etc.)
    val label: String
        get() = (64 + ord).toInt().toChar().toString()
}
