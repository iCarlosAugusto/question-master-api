package com.questionmaster.api.domain.dto.response

/**
 * Response wrapper for encrypted data
 * The client will decrypt this data
 */
data class EncryptedDataResponse(
    val encryptedData: String
)

