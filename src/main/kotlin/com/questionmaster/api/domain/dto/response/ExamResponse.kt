package com.questionmaster.api.domain.dto.response

import java.time.LocalDateTime

data class ExamResponse(
    val id: Long,
    val name: String,
    val slug: String,
    val institution: String?,
    val description: String?,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val questionCount: Int = 0
)

