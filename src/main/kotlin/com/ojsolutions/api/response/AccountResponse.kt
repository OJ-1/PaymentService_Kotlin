package com.ojsolutions.api.response

import com.ojsolutions.api.dto.AccountDto
import com.ojsolutions.domain.ledger.LedgerAccount
import kotlinx.serialization.Serializable

@Serializable
data class AccountResponse(
    val account: AccountDto,
    val ledger: LedgerAccount
)
