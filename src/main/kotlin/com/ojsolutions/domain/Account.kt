package com.ojsolutions.domain

import java.time.LocalDateTime
import java.util.UUID

data class Account(
    val id: UUID,
    val createdDate: LocalDateTime,
    val updatedDate: LocalDateTime,
    val ownerId: UUID,
    val ownerCategory: OwnerCategory,
    val status: AccountStatus,
    val ledger: Ledger,
    val accountNumber: String,
    val accountType: AccountType,
    val description: String
)