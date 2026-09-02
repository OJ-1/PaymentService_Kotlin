package com.ojsolutions.api.dto

import com.ojsolutions.domain.*
import kotlinx.serialization.Serializable

@Serializable
data class PaymentDto (
    val id: String,
    val createdDate: String,
    val updatedDate: String,
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