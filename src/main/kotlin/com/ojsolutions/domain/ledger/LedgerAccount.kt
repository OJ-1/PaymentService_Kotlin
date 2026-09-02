package com.ojsolutions.domain.ledger

import kotlinx.serialization.Serializable

@Serializable
data class LedgerAccount(
    val accountNumber: String,
    val debitsPosted: Long,
    val creditsPosted: Long,
    val balance: Long
)