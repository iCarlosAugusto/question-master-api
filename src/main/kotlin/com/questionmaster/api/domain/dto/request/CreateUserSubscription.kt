package com.questionmaster.api.domain.dto.request

import java.util.UUID

data class CreateUserSubscriptionRequest(
    val userId: UUID,
    val examId: Long,
    val plan: String,
    val stripeCustomerId: String,
    val stripeSubscriptionId: String
)