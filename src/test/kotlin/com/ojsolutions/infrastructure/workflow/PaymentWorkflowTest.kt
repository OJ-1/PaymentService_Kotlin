package com.ojsolutions.infrastructure.workflow

import com.ojsolutions.api.request.CreatePaymentRequest
import com.ojsolutions.api.response.CreatePaymentResponse
import com.ojsolutions.api.response.InvalidRequestException
import com.ojsolutions.domain.Asset
import com.ojsolutions.domain.AssetType
import com.ojsolutions.domain.PaymentType
import dev.restate.client.Client
import dev.restate.client.IngressException
import dev.restate.client.kotlin.workflow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PaymentWorkflowTest {

    private val restateClient = Client.connect("http://localhost:9072")

    private fun uniqueId(): String = UUID.randomUUID().toString().replace("-", "")

    private fun validRequest(
        msgId: String = "workflow-${uniqueId()}",
        amount: Long = 100,
        debtorAccountNumber: String = "1001",
        creditorAccountNumber: String = "1002",
        asset: Asset = Asset.ZAR,
        assetType: AssetType = AssetType.FIAT,
        type: PaymentType = PaymentType.MOBILE_TOP_UP
    ) =
        CreatePaymentRequest(
            msgId = msgId,
            debtorAccountNumber = debtorAccountNumber,
            creditorAccountNumber = creditorAccountNumber,
            amount = amount,
            asset = asset,
            assetType = assetType,
            type = type
        )

    private suspend fun executeWorkflow(request: CreatePaymentRequest): CreatePaymentResponse {

        val workflowId = "payment-${request.msgId}"

        return restateClient.workflow<PaymentWorkflow>(workflowId).run(request)
    }

    private suspend fun executeWorkflowExpectingFailure(request: CreatePaymentRequest): IngressException =
    assertFailsWith<IngressException> {
        executeWorkflow(request)
    }

    private fun assertFailureContains(exception: IngressException, expectedCode: String) {

        val message = exception.message.orEmpty()

        assertTrue(
            message.contains(expectedCode),
            message
        )
    }

    // ========================================================= SUCCESSFUL PAYMENTS \/

    @Test
    fun `Feature - Payment workflow Scenario - Successful payment completes`() =
        runBlocking {

            val request = validRequest(
                msgId = "successful-${uniqueId()}",
                amount = 100
            )

            val response = executeWorkflow(request)

            assertNotNull(
                response.paymentReference
            )

            assertTrue(
                response.paymentReference.isNotBlank()
            )
        }

    @Test
    fun `Feature - Payment workflow Scenario - Successful payment reference is a UUID`() {
        runBlocking {

            val request = validRequest(
                msgId = "reference-${uniqueId()}",
                amount = 100
            )

            val response = executeWorkflow(request)

            assertDoesNotThrow {
                UUID.fromString(
                    response.paymentReference
                )
            }
        }
    }

    // ========================================================= SUCCESSFUL PAYMENTS /\

    // ========================================================= PAYMENT AMOUNT VALIDATION \/

    @Test
    fun `Feature - Payment validation Scenario - Zero amount is rejected`() =
        runBlocking {

            val request = validRequest(
                msgId = "zero-amount-${uniqueId()}",
                amount = 0
            )

            val exception = executeWorkflowExpectingFailure(request)

            assertFailureContains(
                exception,
                "INVALID_TRANSACTION_AMOUNT"
            )
        }

    @Test
    fun `Feature - Payment validation Scenario - Negative amount is rejected`() =
        runBlocking {

            val request = validRequest(
                msgId = "negative-amount-${uniqueId()}",
                amount = -100
            )

            val exception = executeWorkflowExpectingFailure(request)

            assertFailureContains(
                exception,
                "INVALID_TRANSACTION_AMOUNT"
            )
        }

    // ========================================================= PAYMENT AMOUNT VALIDATION /\

    // ========================================================= PARTICIPANT VALIDATION \/

    @Test
    fun `Feature - Payment participant validation Scenario - Same debtor and creditor are rejected`() =
        runBlocking {

            val request = validRequest(
                msgId = "same-account-${uniqueId()}",
                debtorAccountNumber = "1001",
                creditorAccountNumber = "1001"
            )

            val exception = executeWorkflowExpectingFailure(request)

            assertFailureContains(
                exception,
                "INVALID_ACCOUNTS"
            )
        }

    @Test
    fun `Feature - Payment participant validation Scenario - Missing debtor account is rejected`() =
        runBlocking {

            val missingAccount = "missing-debtor-${uniqueId()}"

            val request = validRequest(
                msgId = "missing-debtor-${uniqueId()}",
                debtorAccountNumber = missingAccount,
                creditorAccountNumber = "1002"
            )

            val exception = executeWorkflowExpectingFailure(request)

            assertFailureContains(
                exception,
                "DEBTOR_ACCOUNT_NOT_FOUND"
            )
        }

    @Test
    fun `Feature - Payment participant validation Scenario - Missing creditor account is rejected`() =
        runBlocking {

            val missingAccount = "missing-creditor-${uniqueId()}"

            val request = validRequest(
                msgId = "missing-creditor-${uniqueId()}",
                debtorAccountNumber = "1001",
                creditorAccountNumber = missingAccount
            )

            val exception = executeWorkflowExpectingFailure(request)

            assertFailureContains(
                exception,
                "CREDITOR_ACCOUNT_NOT_FOUND"
            )
        }

    // ========================================================= PARTICIPANT VALIDATION /\

    // ========================================================= RESTATE WORKFLOW IDENTITY \/

    @Test
    fun `Feature - Payment workflow identity Scenario - Same message id cannot invoke workflow method twice`() {
        runBlocking {

            val msgId = "idempotent-${uniqueId()}"

            val request = validRequest(
                msgId = msgId,
                amount = 100
            )

            executeWorkflow(request)

            val exception = assertFailsWith<IngressException> {
                executeWorkflow(request)
            }

            assertTrue(
                exception.message.orEmpty().contains("workflow method was already invoked"),
                exception.message
            )
        }
    }

    @Test
    fun `Feature - Payment workflow identity Scenario - Different message ids create different workflows`() =
        runBlocking {

            val firstRequest = validRequest(
                msgId = "workflow-one-${uniqueId()}",
                amount = 100
            )

            val secondRequest = validRequest(
                msgId = "workflow-two-${uniqueId()}",
                amount = 100
            )

            val firstResponse = executeWorkflow(firstRequest)

            val secondResponse = executeWorkflow(secondRequest)

            assertTrue(
                firstResponse.paymentReference != secondResponse.paymentReference,
                """
                Expected different workflow executions to produce
                different payment references.

                First: ${firstResponse.paymentReference}
                Second: ${secondResponse.paymentReference}
                """.trimIndent()
            )
        }

    // ========================================================= RESTATE WORKFLOW IDENTITY /\

    //===
}