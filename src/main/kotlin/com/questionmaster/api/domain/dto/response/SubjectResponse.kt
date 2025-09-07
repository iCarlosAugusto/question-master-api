package com.questionmaster.api.domain.dto.response

import java.time.LocalDateTime

data class SubjectResponse(
    val id: Long,
    val name: String,
    val createdAt: LocalDateTime,
    val topicsCount: Int = 0
)
