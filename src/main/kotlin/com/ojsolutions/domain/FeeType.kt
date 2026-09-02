package com.ojsolutions.domain

import java.time.LocalDateTime
import java.util.UUID

data class FeeType(
    val id: UUID,
    val createdDate: LocalDateTime,
    val updatedDate: LocalDateTime,
    val paymentType: PaymentType,
    val asset: Asset,
    val rate: String,
    val description: String
)