package com.questionmaster.api.domain.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class CreateTopicRequest(
    @field:NotNull(message = "Subject ID is required")
    @field:Positive(message = "Subject ID must be positive")
    val subjectId: Long,
    
    @field:NotBlank(message = "Topic name is required")
    @field:Size(max = 100, message = "Topic name must not exceed 100 characters")
    val name: String
)
