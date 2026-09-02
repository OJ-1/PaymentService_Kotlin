package com.ojsolutions.infrastructure.ledger

import com.ojsolutions.domain.AccountType
import com.ojsolutions.domain.Ledger
import com.ojsolutions.domain.TransferCodes
import com.ojsolutions.domain.ledger.LedgerTransferBatch
import com.ojsolutions.domain.ledger.LedgerTransferRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TigerBeetleLedgerAdapterIntegrationTest {

    private lateinit var ledger: TigerBeetleLedgerAdapter

    @BeforeEach
    fun setup() {
        ledger = TigerBeetleLedgerAdapter()
    }

    @AfterEach
    fun tearDown() {
        ledger.close()
    }

    private fun uniqueAccountNumber(): String = (System.nanoTime() % 9_000_000_000L + 1_000_000_000L).toString()

    private fun createAccount(accountNumber: String, accountType: AccountType = AccountType.TRANSACTIONAL) {
        ledger.createAccount(
            accountNumber = accountNumber,
            accountType = accountType
        )
    }

    private fun transfer(
        debtor: String,
        creditor: String,
        amount: Long,
        code: TransferCodes,
        reference: String = UUID.randomUUID().toString()
    ): List<String> =
        ledger.createTransfers(
            LedgerTransferBatch(
                transfers = listOf(
                    LedgerTransferRequest(
                        debtorAccount = debtor,
                        creditorAccount = creditor,
                        amount = amount,
                        ledger = Ledger.PAYMENTS,
                        transferCode = code,
                        reference = reference
                    )
                )
            )
        )

    @Test
    fun `Feature - TigerBeetle account creation Scenario - Creating an account makes it available for lookup`() {

        val accountNumber = uniqueAccountNumber()

        createAccount(accountNumber)

        val account = ledger.getAccount(accountNumber)

        assertEquals(
            accountNumber,
            account.accountNumber
        )

        assertEquals(
            0,
            account.balance
        )
    }

    @Test
    fun `Feature - TigerBeetle account creation Scenario - Newly created account starts with zero balance`() {

        val accountNumber = uniqueAccountNumber()

        createAccount(accountNumber)

        val account = ledger.getAccount(accountNumber)

        assertEquals(
            0,
            account.debitsPosted
        )

        assertEquals(
            0,
            account.creditsPosted
        )

        assertEquals(
            0,
            account.balance
        )
    }

    @Test
    fun `Feature - TigerBeetle idempotency Scenario - Creating an existing account succeeds`() {

        val accountNumber = uniqueAccountNumber()

        createAccount(accountNumber)

        createAccount(accountNumber)

        val account = ledger.getAccount(accountNumber)

        assertEquals(
            accountNumber,
            account.accountNumber
        )
    }

    @Test
    fun `Feature - TigerBeetle transfers Scenario - Transfer updates debtor creditor balances and history`() {

        /*
         * Account A is the settlement account.
         *
         * Settlement accounts do not have the
         * DEBITS_MUST_NOT_EXCEED_CREDITS restriction.
         *
         * This allows the settlement account to fund
         * isolated transactional accounts.
         */
        val settlementAccount = uniqueAccountNumber()
        val debtorAccount = uniqueAccountNumber()
        val creditorAccount = uniqueAccountNumber()

        createAccount(
            settlementAccount,
            AccountType.SETTLEMENT
        )

        createAccount(
            debtorAccount,
            AccountType.TRANSACTIONAL
        )

        createAccount(
            creditorAccount,
            AccountType.TRANSACTIONAL
        )

        val fundingAmount = 10_000L
        val paymentAmount = 1_000L

        // Fund the debtor account
        transfer(
            debtor = settlementAccount,
            creditor = debtorAccount,
            amount = fundingAmount,
            code = TransferCodes.FUND_CUSTOMER
        )

        val debtorBefore = ledger.getAccount(debtorAccount)

        val creditorBefore = ledger.getAccount(creditorAccount)

        val paymentReference = UUID.randomUUID().toString()

        val references = transfer(
            debtor = debtorAccount,
            creditor = creditorAccount,
            amount = paymentAmount,
            code = TransferCodes.PAYMENT,
            reference = paymentReference
        )

        assertEquals(
            listOf(paymentReference),
            references
        )

        val debtorAfter = ledger.getAccount(debtorAccount)

        val creditorAfter = ledger.getAccount(creditorAccount)

        assertEquals(
            debtorBefore.balance - paymentAmount,
            debtorAfter.balance
        )

        assertEquals(
            creditorBefore.balance + paymentAmount,
            creditorAfter.balance
        )

        val transfers = ledger.getTransfers(accountNumber = creditorAccount)

        assertTrue(
            transfers.any {
                it.transferId == paymentReference &&
                it.debitAccountNumber == debtorAccount &&
                it.creditAccountNumber == creditorAccount &&
                it.amount == paymentAmount &&
                it.code == TransferCodes.PAYMENT.id &&
                it.ledger == Ledger.PAYMENTS.id
            }
        )
    }

    @Test
    fun `Feature - TigerBeetle transfer idempotency Scenario - Same transfer reference can be retried safely`() {

        val settlementAccount = uniqueAccountNumber()
        val debtorAccount = uniqueAccountNumber()
        val creditorAccount = uniqueAccountNumber()

        createAccount(
            settlementAccount,
            AccountType.SETTLEMENT
        )

        createAccount(
            debtorAccount,
            AccountType.TRANSACTIONAL
        )

        createAccount(
            creditorAccount,
            AccountType.TRANSACTIONAL
        )

        // Fund the isolated debtor account
        transfer(
            debtor = settlementAccount,
            creditor = debtorAccount,
            amount = 10_000L,
            code = TransferCodes.FUND_CUSTOMER
        )

        val amount = 1_000L

        val transferReference = UUID.randomUUID().toString()

        val transfer = LedgerTransferRequest(
            debtorAccount = debtorAccount,
            creditorAccount = creditorAccount,
            amount = amount,
            ledger = Ledger.PAYMENTS,
            transferCode = TransferCodes.PAYMENT,
            reference = transferReference
        )

        val debtorBefore = ledger.getAccount(debtorAccount)

        val creditorBefore = ledger.getAccount(creditorAccount)

        val firstReferences = ledger.createTransfers(
            LedgerTransferBatch(
                transfers = listOf(transfer)
            )
        )

        val debtorAfterFirst = ledger.getAccount(debtorAccount)

        val creditorAfterFirst = ledger.getAccount(creditorAccount)

        val secondReferences = ledger.createTransfers(
            LedgerTransferBatch(
                transfers = listOf(transfer)
            )
        )

        val debtorAfterSecond = ledger.getAccount(debtorAccount)

        val creditorAfterSecond = ledger.getAccount(creditorAccount)

        assertEquals(
            listOf(transferReference),
            firstReferences
        )

        assertEquals(
            listOf(transferReference),
            secondReferences
        )

        assertEquals(
            debtorBefore.balance - amount,
            debtorAfterFirst.balance
        )

        assertEquals(
            creditorBefore.balance + amount,
            creditorAfterFirst.balance
        )

        assertEquals(
            debtorAfterFirst.balance,
            debtorAfterSecond.balance
        )

        assertEquals(
            creditorAfterFirst.balance,
            creditorAfterSecond.balance
        )

        val transfers = ledger.getTransfers(accountNumber = creditorAccount)

        assertEquals(
            1,
            transfers.count {
                it.transferId == transferReference
            }
        )
    }
}