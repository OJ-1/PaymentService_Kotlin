package com.ojsolutions.domain

import java.time.LocalDateTime
import java.util.UUID

data class Payment(
    val id: UUID,
    val createdDate: LocalDateTime,
    val updatedDate: LocalDateTime,
    val status: PaymentStatus,
    val paymentReference: String,
    val feeReference: String?,
    val debtorAccount: String,
    val creditorAccount: String,
    val feeAccount: String?,
    val paymentType: PaymentType,
    val amount: Long,
    val asset: Asset,
    val assetType: AssetType,
    val feeRate: String,
    val feeAmount: Long
)