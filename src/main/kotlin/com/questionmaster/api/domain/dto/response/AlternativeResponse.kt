package com.questionmaster.api.domain.dto.response

import java.util.*

data class AlternativeResponse(
    val id: UUID,
    val body: String,
    val label: String,
    val ord: Short
)
