package com.ojsolutions.api.response

import kotlinx.serialization.Serializable

@Serializable
data class CreateAccountResponse(
    val accountNumber: String
)