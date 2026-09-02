package com.ojsolutions.api.response

import kotlinx.serialization.Serializable

@Serializable
data class CreatePaymentResponse(
    val paymentReference: String,
    val feeReference: String? = null
)