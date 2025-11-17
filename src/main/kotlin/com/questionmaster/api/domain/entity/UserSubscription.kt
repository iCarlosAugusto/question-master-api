package com.questionmaster.api.domain.entity

import com.questionmaster.api.domain.dto.response.UserSubscriptionResponse
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "user_subscriptions")
class UserSubscription (

    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    val exam: Exam,

    @Column()
    val plan: String,

    @Column(length = 20)
    val status: String = "INACTIVE",

    @Column()
    val stripeCustomerId: String,

    @Column()
    val stripeSubscriptionId: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    ){

    fun toResponse(): UserSubscriptionResponse {
        return UserSubscriptionResponse(
            id = id,
            userId = user.id,
            examId = exam.id,
            plan = plan,
            status = status,
            stripeCustomerId = stripeCustomerId,
            stripeSubscriptionId = stripeSubscriptionId,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}