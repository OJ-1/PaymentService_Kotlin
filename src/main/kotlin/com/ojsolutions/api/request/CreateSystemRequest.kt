package com.ojsolutions.api.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateSystemRequest(
    val msgId: String,
    val name: String,
    val description: String
)
