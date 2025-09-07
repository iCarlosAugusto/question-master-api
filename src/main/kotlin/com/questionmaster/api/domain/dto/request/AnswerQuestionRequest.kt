package com.questionmaster.api.domain.dto.request

import jakarta.validation.constraints.NotNull
import java.util.*

data class AnswerQuestionRequest(
    @field:NotNull(message = "Alternative ID is required")
    val alternativeId: UUID
)
