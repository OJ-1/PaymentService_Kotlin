package com.ojsolutions.api.request

import com.ojsolutions.domain.AccountStatus
import com.ojsolutions.domain.AccountType
import kotlinx.serialization.Serializable

@Serializable
data class UpdateAccountRequest (
    val msgId: String,
    val status: AccountStatus?  = null,
    val description: String? = null
)