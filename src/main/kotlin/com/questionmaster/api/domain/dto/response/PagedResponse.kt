package com.questionmaster.api.domain.dto.response

data class PagedResponse<T>(
    val items: List<T>,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int,
    val totalItems: Long
)
