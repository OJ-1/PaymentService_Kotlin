package com.ojsolutions.api.response

import kotlinx.serialization.Serializable

@Serializable
data class AccountsResponse(
    val accounts: List<AccountResponse>
)