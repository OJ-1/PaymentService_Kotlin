package com.ojsolutions.domain.ledger

data class LedgerTransferBatch(
    val transfers: List<LedgerTransferRequest>
)