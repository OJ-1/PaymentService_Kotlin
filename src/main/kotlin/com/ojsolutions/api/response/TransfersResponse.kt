package com.ojsolutions.api.response

import com.ojsolutions.api.dto.AccountDto
import com.ojsolutions.domain.ledger.LedgerTransfer
import kotlinx.serialization.Serializable

@Serializable
data class TransfersResponse(
    val account: AccountDto,
    val transfers: List<LedgerTransfer>
)