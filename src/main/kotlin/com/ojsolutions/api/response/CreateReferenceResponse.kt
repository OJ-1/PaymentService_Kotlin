package com.ojsolutions.api.response

import kotlinx.serialization.Serializable

@Serializable
data class CreateReferenceResponse(
    val reference: String
)