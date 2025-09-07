package com.questionmaster.api.domain.dto.response

import java.time.LocalDateTime
import java.util.*

data class AnswerResponse(
    val id: UUID,
    val questionId: UUID,
    val alternativeId: UUID,
    val isCorrect: Boolean,
    val answeredAt: LocalDateTime,
    val correctAlternativeId: UUID
)
