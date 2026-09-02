package com.ojsolutions.api.response

import kotlinx.serialization.Serializable

@Serializable
data class ApiError(
    val code: String,
    val description: String
)