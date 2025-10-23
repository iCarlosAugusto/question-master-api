package com.questionmaster.api.domain.entity

import com.questionmaster.api.domain.enums.QuestionType
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "questions")
class Question(
    @Id
    val id: UUID = UUID.randomUUID(),
    
    @Column(nullable = false, columnDefinition = "TEXT")
    val statement: String,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    val subject: Subject,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id")
    val exam: Exam? = null,
    
    val year: Short? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "qtype", nullable = false)
    val questionType: QuestionType,
    
    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    val createdBy: User? = null,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "question_topics",
        joinColumns = [JoinColumn(name = "question_id")],
        inverseJoinColumns = [JoinColumn(name = "topic_id")]
    )
    val topics: MutableSet<Topic> = mutableSetOf(),
    
    @OneToMany(mappedBy = "question", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val alternatives: MutableSet<Alternative> = mutableSetOf(),
    
    @OneToMany(mappedBy = "question", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val answers: MutableSet<Answer> = mutableSetOf()
) {
    // JPA requires a no-arg constructor
    constructor() : this(
        statement = "",
        subject = Subject(),
        questionType = QuestionType.MULTIPLE_CHOICE
    )

    fun copy(
        id: UUID = this.id,
        statement: String = this.statement,
        subject: Subject = this.subject,
        exam: Exam? = this.exam,
        year: Short? = this.year,
        questionType: QuestionType = this.questionType,
        isActive: Boolean = this.isActive,
        createdBy: User? = this.createdBy,
        createdAt: LocalDateTime = this.createdAt,
        updatedAt: LocalDateTime = this.updatedAt,
        topics: MutableSet<Topic> = this.topics,
        alternatives: MutableSet<Alternative> = this.alternatives,
        answers: MutableSet<Answer> = this.answers
    ): Question {
        return Question(
            id = id,
            statement = statement,
            subject = subject,
            exam = exam,
            year = year,
            questionType = questionType,
            isActive = isActive,
            createdBy = createdBy,
            createdAt = createdAt,
            updatedAt = updatedAt,
            topics = topics,
            alternatives = alternatives,
            answers = answers
        )
    }
}
