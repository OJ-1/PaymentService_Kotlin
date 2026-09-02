package com.ojsolutions.domain.ledger

import kotlinx.serialization.Serializable

@Serializable
data class LedgerTransfer(
    val transferId: String,
    val timestamp: Long,
    val debitAccountNumber: String,
    val creditAccountNumber: String,
    val amount: Long,
    val code: Int,
    val ledger: Int
)