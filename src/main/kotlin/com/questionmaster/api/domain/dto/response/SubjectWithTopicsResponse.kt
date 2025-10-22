package com.questionmaster.api.domain.dto.response

data class SubjectWithTopicsResponse(
    val id: Long,
    val name: String,
    val topics: List<TopicSummaryResponse>
)

data class TopicSummaryResponse(
    val id: Long,
    val name: String
)

