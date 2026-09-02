package com.ojsolutions.api.request

import com.ojsolutions.domain.*
import kotlinx.serialization.Serializable

@Serializable
data class CreatePaymentRequest(
    val msgId: String,
    val debtorAccountNumber: String,
    val creditorAccountNumber: String,
    val amount: Long,
    val asset: Asset,
    val assetType: AssetType,
    val type: PaymentType
)