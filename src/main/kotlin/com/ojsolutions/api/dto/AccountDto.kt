package com.ojsolutions.api.dto

import com.ojsolutions.domain.AccountStatus
import com.ojsolutions.domain.AccountType
import com.ojsolutions.domain.Ledger
import com.ojsolutions.domain.OwnerCategory
import kotlinx.serialization.Serializable

@Serializable
data class AccountDto(
    val id: String,
    val ownerId: String,
    val ownerCategory: OwnerCategory,
    val createdDate: String,
    val updatedDate: String,
    val status: AccountStatus,
    val ledger: Ledger,
    val accountNumber: String,
    val accountType: AccountType,
    val description: String
)