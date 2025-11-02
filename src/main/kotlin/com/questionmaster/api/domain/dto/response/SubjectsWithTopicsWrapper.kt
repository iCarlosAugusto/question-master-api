package com.questionmaster.api.domain.dto.response

data class SubjectsWithTopicsWrapper(
    val exam: ExamResponse,
    val subjects: List<SubjectWithTopicsResponse>
)

