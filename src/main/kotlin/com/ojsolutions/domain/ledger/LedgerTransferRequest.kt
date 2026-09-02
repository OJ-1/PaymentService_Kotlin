package com.ojsolutions.domain.ledger

import com.ojsolutions.domain.Ledger
import com.ojsolutions.domain.TransferCodes

data class LedgerTransferRequest(
    val debtorAccount: String,
    val creditorAccount: String,
    val amount: Long,
    val ledger: Ledger,
    val transferCode: TransferCodes,
    val reference: String
)