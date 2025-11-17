package com.questionmaster.api.domain.dto.response

import java.time.LocalDateTime
import java.util.UUID

data class UserSubscriptionResponse(
    val id: UUID,
    val userId: UUID,
    val examId: Long,
    val plan: String,
    val status: String,
    val stripeCustomerId: String,
    val stripeSubscriptionId: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)