package com.ojsolutions.api.response

import com.ojsolutions.domain.ledger.LedgerTransfer
import kotlinx.serialization.Serializable

@Serializable
data class TransferResponse(
    val debitAccount: String,
    val creditAccount: String,
    val transfer: LedgerTransfer
)