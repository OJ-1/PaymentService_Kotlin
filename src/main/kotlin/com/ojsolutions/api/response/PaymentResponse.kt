package com.ojsolutions.api.response

import com.ojsolutions.api.dto.PaymentDto
import kotlinx.serialization.Serializable

@Serializable
data class PaymentResponse(
    val payment: PaymentDto
)