package com.ojsolutions.api.request

import com.ojsolutions.domain.*
import kotlinx.serialization.Serializable

@Serializable
data class CreateAccountRequest(
    val msgId: String,
    val ownerId: String,
    val ownerCategory: OwnerCategory,
    val ledger: Ledger,
    val accountType: AccountType,
    val description: String
)