package com.ojsolutions.api.response

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val resultCode: ResultCode,
    val data: T? = null,
    val errors: List<ApiError> = emptyList()
)