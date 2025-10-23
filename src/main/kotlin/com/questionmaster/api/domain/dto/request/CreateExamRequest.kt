package com.questionmaster.api.domain.dto.request

import com.questionmaster.api.domain.enums.ExamType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class CreateExamRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 200, message = "Name must not exceed 200 characters")
    val name: String,

    @field:Size(max = 20, message = "Must have a slug")
    val slug: String,
    
    @field:Size(max = 100, message = "Institution must not exceed 100 characters")
    val institution: String? = null,
    
    val description: String? = null
)

