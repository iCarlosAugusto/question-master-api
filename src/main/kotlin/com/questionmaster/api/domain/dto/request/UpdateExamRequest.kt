package com.questionmaster.api.domain.dto.request

import jakarta.validation.constraints.Size

data class UpdateExamRequest(
    @field:Size(max = 200, message = "Name must not exceed 200 characters")
    val name: String? = null,

    @field:Size(max = 100, message = "Institution must not exceed 100 characters")
    val institution: String? = null,
    
    val description: String? = null,
    
    val isActive: Boolean? = null
)

