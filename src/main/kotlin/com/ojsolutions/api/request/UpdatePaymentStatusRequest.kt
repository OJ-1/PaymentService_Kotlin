package com.ojsolutions.api.request

import com.ojsolutions.domain.OwnerStatus
import com.ojsolutions.domain.PaymentStatus
import kotlinx.serialization.Serializable

@Serializable
data class UpdatePaymentStatusRequest (
    val msgId: String,
    val status: PaymentStatus
)