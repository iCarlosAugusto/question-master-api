package com.questionmaster.api.domain.dto.response

import java.time.LocalDateTime

data class TopicResponse(
    val id: Long,
    val name: String,
    val subjectId: Long,
    val subjectName: String,
    val createdAt: LocalDateTime
)
