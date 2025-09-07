package com.questionmaster.api.domain.dto.response

import com.questionmaster.api.domain.enums.AppRole
import java.util.*

data class AuthResponse(
    val token: String,
    val userId: UUID,
    val role: AppRole,
    val displayName: String?
)
