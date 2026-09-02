package com.ojsolutions.infrastructure.ledger

import com.ojsolutions.domain.AccountType
import com.ojsolutions.domain.port.LedgerPort

object LedgerSeeder {

    fun seed(ledgerPort: LedgerPort) {

        ledgerPort.createAccount(
            accountNumber = "1",
            accountType = AccountType.SETTLEMENT
        )

        ledgerPort.createAccount(
            accountNumber = "2",
            accountType = AccountType.FEE
        )

        ledgerPort.createAccount(
            accountNumber = "1001",
            accountType = AccountType.TRANSACTIONAL
        )

        ledgerPort.createAccount(
            accountNumber = "1002",
            accountType = AccountType.TRANSACTIONAL
        )

        ledgerPort.createAccount(
            accountNumber = "2001",
            accountType = AccountType.TRANSACTIONAL
        )

        ledgerPort.createAccount(
            accountNumber = "2002",
            accountType = AccountType.TRANSACTIONAL
        )

        println("TigerBeetle seeded successfully.")
    }
}