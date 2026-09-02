package com.ojsolutions.infrastructure.workflow

import com.ojsolutions.api.dto.FeeRateWorkflowDto
import com.ojsolutions.api.request.CreatePaymentRequest
import com.ojsolutions.api.response.CreatePaymentResponse
import com.ojsolutions.api.response.FeeConfigurationNotFoundException
import com.ojsolutions.domain.AccountStatus
import com.ojsolutions.domain.Ledger
import com.ojsolutions.domain.LedgerTransferResult
import com.ojsolutions.domain.Payment
import com.ojsolutions.domain.PaymentStatus
import com.ojsolutions.domain.TransferCodes
import com.ojsolutions.domain.ledger.*
import com.ojsolutions.domain.ledger.InsufficientFundsException
import com.ojsolutions.domain.ledger.LedgerTransferBatch
import com.ojsolutions.domain.ledger.LedgerTransferRequest
import com.ojsolutions.domain.port.LedgerPort
import com.ojsolutions.infrastructure.database.repository.AccountRepository
import com.ojsolutions.infrastructure.database.repository.FeeTypeRepository
import com.ojsolutions.infrastructure.database.repository.PaymentRepository
import dev.restate.sdk.annotation.Handler
import dev.restate.sdk.annotation.Workflow
import dev.restate.sdk.common.TerminalException
import dev.restate.sdk.kotlin.runBlock
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.util.UUID

@Workflow
open class PaymentWorkflow(
    private val accountRepository: AccountRepository,
    private val feeTypeRepository: FeeTypeRepository,
    private val ledgerPort: LedgerPort,
    private val paymentRepository: PaymentRepository
) {

    @Handler
    open suspend fun run(request: CreatePaymentRequest): CreatePaymentResponse {

        val paymentId = runBlock("generate-payment-id") {
            UUID.randomUUID().toString()
        }

        val now = runBlock("generate-payment-time") {
            LocalDateTime.now().toString()
        }

        val feeAccount = "2" //**** hardcoded for now - future update

        // ------------------------- VALIDATION \/

        // Check the payment amount is greater than 0
        if (request.amount <= 0) {
            throw TerminalException(
                "INVALID_TRANSACTION_AMOUNT: Transaction amount must be greater than 0."
            )
        }

        // Check the debtor and creditor account are not the same
        if (request.debtorAccountNumber == request.creditorAccountNumber) {
            throw TerminalException(
                "INVALID_ACCOUNTS: Debtor Account and Creditor Account cannot be the same."
            )
        }

        // Check the debtor account exists
        val debtorExists = runBlock("validate-debtor-account") {
            accountRepository.existsByAccountNumber(
                request.debtorAccountNumber
            )
        }

        if (!debtorExists) {
            throw TerminalException(
                "DEBTOR_ACCOUNT_NOT_FOUND: Debtor Account does not exist."
            )
        }

        // Check the creditor account exists
        val creditorExists = runBlock("validate-creditor-account") {
            accountRepository.existsByAccountNumber(
                request.creditorAccountNumber
            )
        }

        if (!creditorExists) {
            throw TerminalException(
                "CREDITOR_ACCOUNT_NOT_FOUND: Creditor Account does not exist."
            )
        }

        val debtor = runBlock("get-debtor-account") {
            accountRepository.get(
                request.debtorAccountNumber
            ).status.name
        }

        // Check if the debtor account status is ACTIVE
        if (debtor != AccountStatus.ACTIVE.name) {
            throw TerminalException(
                "DEBTOR_ACCOUNT_NOT_ACTIVE: Debtor Account is not active."
            )
        }

        // Check if the creditor account status is ACTIVE
        val creditor = runBlock("get-creditor-account") {
            accountRepository.get(
                request.creditorAccountNumber
            ).status.name
        }

        if (creditor != AccountStatus.ACTIVE.name) {
            throw TerminalException(
                "CREDITOR_ACCOUNT_NOT_ACTIVE: Creditor Account is not active."
            )
        }

        // ------------------------- VALIDATION /\

        // ------------------------- FEE \/

        val fee = runBlock("get-payment-fee") {
            try {
                val feeType = feeTypeRepository.get(
                    request.type,
                    request.asset
                )

                FeeRateWorkflowDto(
                    rate = feeType.rate
                )
            } catch (ex: FeeConfigurationNotFoundException) {
                throw TerminalException(
                    "FEE_CONFIGURATION_NOT_FOUND: ${
                        ex.message ?: "Fee configuration not found."
                    }"
                )
            }
        }

        // Convert the fee amount to decimal and make sure it is a whole int
        // Note rounds a number to the nearest neighbor unless both neighbors are equidistant, in which case it rounds up (away from zero for positive numbers).
        val feeAmount = BigDecimal(request.amount)
            .multiply(BigDecimal(fee.rate))
            .setScale(0, RoundingMode.HALF_UP)
            .toLong()

        // ------------------------- FEE /\

        // ------------------------- REFERENCES \/

        // Generate the Payment Reference
        val paymentRef = runBlock("get-payment-reference") {
            UUID.randomUUID().toString()
        }

        // Generate the Fee Reference (only used if there is a fee payment)
        val feeRef = runBlock("generate-fee-reference") {
            if (feeAmount > 0) {
                UUID.randomUUID().toString()
            } else {
                ""
            }
        }

        // ------------------------- REFERENCES /\

        // Create the payment domain object
        val payment = Payment(
            id = UUID.fromString(paymentId),
            createdDate = LocalDateTime.parse(now),
            updatedDate = LocalDateTime.parse(now),
            status = PaymentStatus.PENDING,
            paymentReference = paymentRef,
            feeReference = feeRef.ifBlank { null },
            debtorAccount = request.debtorAccountNumber,
            creditorAccount = request.creditorAccountNumber,
            feeAccount = if (feeAmount > 0) feeAccount else null,
            paymentType = request.type,
            amount = request.amount,
            asset = request.asset,
            assetType = request.assetType,
            feeRate = fee.rate,
            feeAmount = feeAmount
        )

        // CREATE PAYMENT IN DB
        // NOTE: Status is initiated as PENDING
        runBlock("create-payment") {
            paymentRepository.create(payment)
        }

        // ------------------------- LEDGER \/

        val transfers = mutableListOf(
            LedgerTransferRequest(
                debtorAccount = request.debtorAccountNumber,
                creditorAccount = request.creditorAccountNumber,
                amount = request.amount,
                ledger = Ledger.PAYMENTS,
                transferCode = TransferCodes.PAYMENT,
                reference = paymentRef
            )
        )

        // Check if the fee amount is greater than 0 - only then proceed to include the fee payment
        if (feeAmount > 0) {
            transfers += LedgerTransferRequest(
                debtorAccount = request.debtorAccountNumber,
                creditorAccount = feeAccount,
                amount = feeAmount,
                ledger = Ledger.PAYMENTS,
                transferCode = TransferCodes.FEE,
                reference = feeRef
            )
        }

        val batch = LedgerTransferBatch(transfers)

        val ledgerResult = runBlock("create-payment-transfers") {
            try {
                ledgerPort.createTransfers(batch)
                LedgerTransferResult.SUCCESS
            } catch (ex: InsufficientFundsException) {
                LedgerTransferResult.INSUFFICIENT_FUNDS
            } catch (ex: DebtorAccountNotFoundException) {
                LedgerTransferResult.DEBTOR_ACCOUNT_NOT_FOUND
            } catch (ex: CreditorAccountNotFoundException) {
                LedgerTransferResult.CREDITOR_ACCOUNT_NOT_FOUND
            }
        }

        when (ledgerResult) {

            LedgerTransferResult.SUCCESS -> {
                // Continue with successful payment processing.
            }

            LedgerTransferResult.INSUFFICIENT_FUNDS -> {

                runBlock("mark-payment-rejected") {
                    paymentRepository.updateStatus(
                        paymentId = payment.id,
                        updatedStatus = PaymentStatus.REJECTED
                    )
                }

                throw TerminalException(
                    "INSUFFICIENT_FUNDS: Customer account has insufficient funds."
                )
            }

            LedgerTransferResult.DEBTOR_ACCOUNT_NOT_FOUND -> {

                runBlock("mark-payment-rejected") {
                    paymentRepository.updateStatus(
                        paymentId = payment.id,
                        updatedStatus = PaymentStatus.REJECTED
                    )
                }

                throw TerminalException(
                    "DEBTOR_ACCOUNT_NOT_FOUND: Debtor account not found."
                )
            }

            LedgerTransferResult.CREDITOR_ACCOUNT_NOT_FOUND -> {

                runBlock("mark-payment-rejected") {
                    paymentRepository.updateStatus(
                        paymentId = payment.id,
                        updatedStatus = PaymentStatus.REJECTED
                    )
                }

                throw TerminalException(
                    "CREDITOR_ACCOUNT_NOT_FOUND: Creditor account not found."
                )
            }
        }

        // ------------------------- LEDGER /\

        // PERSIST PAYMENT TERMINAL STATUS
        runBlock("mark-payment-success") {
            paymentRepository.updateStatus(
                payment.id,
                PaymentStatus.SUCCESS
            )
        }

        return CreatePaymentResponse(
            paymentReference = paymentRef,
            feeReference = feeRef.ifBlank { null }
        )
    }
}