package com.questionmaster.api.domain.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class CreateUserSubscriptionRequest(

    @field:NotBlank(message = "Id is required")
    @field:Size(max = 150, message = "ID must not exceed 150 characters")
    val userId: UUID,
    val examId: Long,
    val plan: String,
    val stripeCustomerId: String,
    val stripeSubscriptionId: String
)