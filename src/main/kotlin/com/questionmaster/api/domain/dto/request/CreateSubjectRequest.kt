package com.questionmaster.api.domain.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateSubjectRequest(
    @field:NotBlank(message = "Subject name is required")
    @field:Size(max = 100, message = "Subject name must not exceed 100 characters")
    val name: String
)
