package com.ojsolutions.infrastructure.ledger

import com.ojsolutions.domain.ledger.*
import com.tigerbeetle.AccountBatch
import com.tigerbeetle.AccountFilter
import com.tigerbeetle.AccountFlags
import com.tigerbeetle.Client
import com.tigerbeetle.CreateAccountStatus
import com.tigerbeetle.CreateTransferStatus
import com.tigerbeetle.IdBatch
import com.tigerbeetle.TransferBatch
import com.tigerbeetle.UInt128
import com.ojsolutions.domain.port.LedgerPort
import com.ojsolutions.domain.AccountType
import com.ojsolutions.domain.Ledger
import com.tigerbeetle.TransferFlags
import java.util.UUID

class TigerBeetleLedgerAdapter : LedgerPort, AutoCloseable {

    //======================================== HELPERS \/

    //**** Left in for debugging
    fun debugAccount(accountNumber: String) {

        val ids = IdBatch(1)

        ids.add()
        ids.setId(accountId(accountNumber))

        val accounts = client.lookupAccounts(ids)

        if (!accounts.next()) {
            println("Account '$accountNumber' NOT FOUND")
            return
        }

        println("===================================")
        println("Account Number : $accountNumber")
        println("Ledger         : ${accounts.ledger}")
        println("Code           : ${accounts.code}")
        println("Debits Posted  : ${accounts.debitsPosted}")
        println("Credits Posted : ${accounts.creditsPosted}")
        println("Flags          : ${accounts.flags}")
        println("Timestamp      : ${accounts.timestamp}")
        println("===================================")
    }

    // Application account number ("1001") -> TigerBeetle UInt128
    private fun accountId(accountNumber: String): ByteArray = UInt128.asBytes(accountNumber.toLong())

    // TigerBeetle UInt128 -> Application account number ("1001")
    private fun ByteArray.toAccountNumber(): String = UInt128.asBigInteger(this).toString()

    // Transfer UUID string -> TigerBeetle UInt128
    private fun transferId(reference: String): ByteArray = UInt128.asBytes(UUID.fromString(reference))

    // TigerBeetle UInt128 -> Transfer UUID string
    private fun ByteArray.toTransferReference(): String = UInt128.asUUID(this).toString()

    //======================================== HELPERS /\

    // Create TigerBeetle client
    private val clusterId = UInt128.asBytes(0)

    private val tigerBeetleHost = System.getenv("TIGERBEETLE_HOST") ?: "127.0.0.1"

    private val tigerBeetlePort = System.getenv("TIGERBEETLE_PORT") ?: "3000"

    private val client = Client(
        clusterId,
        arrayOf("$tigerBeetleHost:$tigerBeetlePort")
    )

    // CREATE ACCOUNT
    override fun createAccount(accountNumber: String, accountType: AccountType) {

        val accounts = AccountBatch(1)

        accounts.add()

        accounts.id = accountId(accountNumber)
        accounts.ledger = Ledger.PAYMENTS.id
        accounts.code = 1

        val flags = when (accountType) {
            AccountType.SETTLEMENT -> AccountFlags.NONE
            else -> AccountFlags.DEBITS_MUST_NOT_EXCEED_CREDITS or AccountFlags.HISTORY
        }

        accounts.flags = flags

        accounts.setUserData128(0, 0)
        accounts.userData64 = 0
        accounts.userData32 = 0
        accounts.timestamp = 0

        val results = client.createAccounts(accounts)

        while (results.next()) {
            when (results.status) {

                // Success
                // Exists
                CreateAccountStatus.Created,
                CreateAccountStatus.Exists -> {
                    // Continue
                }
                else ->
                    throw LedgerOperationException(
                        results.status.toString()
                    )
            }
        }
    }

    // GET ACCOUNT
    // TigerBeetle Adapter only returns Account balance
    override fun getAccount(accountNumber: String): LedgerAccount {
        val ids = IdBatch(1)

        ids.add()
        ids.setId(accountId(accountNumber))

        val account = client.lookupAccounts(ids)

        if (!account.next()) {
            throw AccountNotFoundException()
        }

        return LedgerAccount(
            accountNumber = accountNumber,
            debitsPosted = account.debitsPosted.toLong(),
            creditsPosted = account.creditsPosted.toLong(),
            balance = account.creditsPosted.toLong() - account.debitsPosted.toLong()
        )
    }

    // CREATE TRANSFERS
    override fun createTransfers(batch: LedgerTransferBatch): List<String> {

        val transferBatch = TransferBatch(batch.transfers.size)

        val references = mutableListOf<String>()

        batch.transfers.forEachIndexed { index, transfer ->

            references += transfer.reference

            transferBatch.add()

            transferBatch.id = transferId(transfer.reference)
            transferBatch.debitAccountId = accountId(transfer.debtorAccount)
            transferBatch.creditAccountId = accountId(transfer.creditorAccount)
            transferBatch.amount = transfer.amount.toBigInteger()
            transferBatch.ledger = transfer.ledger.id
            transferBatch.code = transfer.transferCode.id

            if (index < batch.transfers.lastIndex) {
                transferBatch.flags = TransferFlags.LINKED
            }
        }

        val results = client.createTransfers(transferBatch)

        while (results.next()) {

            when (results.status) {

                CreateTransferStatus.Created,
                CreateTransferStatus.Exists -> {
                    // Transfer successfully exists.
                }

                CreateTransferStatus.ExceedsCredits -> throw InsufficientFundsException()
                CreateTransferStatus.DebitAccountNotFound -> throw DebtorAccountNotFoundException()
                CreateTransferStatus.CreditAccountNotFound -> throw CreditorAccountNotFoundException()
                else -> throw LedgerOperationException(
                    results.status.toString()
                )
            }
        }

        return references
    }

    // GET TRANSFERS
    override fun getTransfers(accountNumber: String, from: Long?, to: Long?): List<LedgerTransfer> {

        val filter = AccountFilter()

        filter.accountId = accountId(accountNumber)
        filter.timestampMin = from ?: 0
        filter.timestampMax = to ?: Long.MAX_VALUE
        filter.limit = 1000
        filter.debits = true
        filter.credits = true
        filter.reversed = false

        val transfers = client.getAccountTransfers(filter)

        val results = mutableListOf<LedgerTransfer>()

        while (transfers.next()) {
            results += LedgerTransfer(
                transferId = transfers.id.toTransferReference(),
                timestamp = transfers.timestamp,
                debitAccountNumber =  transfers.debitAccountId.toAccountNumber(),
                creditAccountNumber = transfers.creditAccountId.toAccountNumber(),
                amount = transfers.amount.toLong(),
                code = transfers.code,
                ledger = transfers.ledger
            )
        }
        return results
    }

    // Gracefully close the ledger client
    override fun close() {
        client.close()
    }

}