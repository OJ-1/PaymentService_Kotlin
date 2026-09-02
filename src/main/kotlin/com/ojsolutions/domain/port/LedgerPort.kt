package com.ojsolutions.domain.port

import com.ojsolutions.domain.ledger.LedgerTransferBatch
import com.ojsolutions.domain.AccountType
import com.ojsolutions.domain.ledger.*

// This is the Ledger Port - this interface defines the logic to be implemented by any Ledger
interface LedgerPort {

    // CreateAccount
    fun createAccount(
        accountNumber: String,
        accountType: AccountType = AccountType.TRANSACTIONAL
    )

    // GetAccount
    fun getAccount(
        accountNumber: String
    ): LedgerAccount

    // CreateTransfers
    fun createTransfers(
        batch: LedgerTransferBatch
    ): List<String>

    // GetTransfers
    fun getTransfers(
        accountNumber: String,
        from: Long? = null,
        to: Long? = null
    ): List<LedgerTransfer>
}