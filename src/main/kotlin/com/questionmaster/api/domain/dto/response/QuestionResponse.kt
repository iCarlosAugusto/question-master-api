package com.questionmaster.api.domain.dto.response

import com.questionmaster.api.domain.enums.QuestionType
import java.time.LocalDateTime
import java.util.*

data class QuestionResponse(
    val id: UUID,
    val statement: String,
    val subject: SubjectResponse,
//    val topics: List<TopicResponse>,
    val exam: ExamSummaryResponse? = null,
    val year: Short? = null,
    val questionType: QuestionType,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val alternatives: List<AlternativeResponse>,
    val userAnswer: UserAnswerInfo? = null
)

data class UserAnswerInfo(
    val answerId: UUID,
    val chosenAlternativeId: UUID,
    val isCorrect: Boolean,
    val answeredAt: LocalDateTime
)
