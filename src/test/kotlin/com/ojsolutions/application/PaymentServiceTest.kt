package com.ojsolutions.application

import com.ojsolutions.api.request.CreatePaymentRequest
import com.ojsolutions.api.response.CreatePaymentResponse
import com.ojsolutions.api.response.ResultCode
import com.ojsolutions.domain.Asset
import com.ojsolutions.domain.AssetType
import com.ojsolutions.domain.PaymentType
import com.ojsolutions.domain.port.WorkflowPort
import com.ojsolutions.infrastructure.database.repository.PaymentRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PaymentServiceTest {

    private val workflowPort = mockk<WorkflowPort>()
    private val paymentRepository = mockk<PaymentRepository>(relaxed = true)

    private val paymentService = PaymentService(
        workflowPort = workflowPort,
        paymentRepository = paymentRepository
    )

    private fun validRequest() =
        CreatePaymentRequest(
            msgId = "test-message-001",
            debtorAccountNumber = "1001",
            creditorAccountNumber = "1002",
            amount = 10_000,
            asset = Asset.ZAR,
            assetType = AssetType.FIAT,
            type = PaymentType.MOBILE_TOP_UP
        )

    @Test
    suspend fun `Feature - Create payment Scenario - Successful workflow response is returned in success response`() {

        val request = validRequest()

        val expectedResponse = CreatePaymentResponse(
            paymentReference = "11111111-1111-1111-1111-111111111111",
            feeReference = "22222222-2222-2222-2222-222222222222"
        )

        coEvery {
            workflowPort.processPayment(request)
        } returns expectedResponse

        val response = paymentService.createPayment(request)

        assertEquals(
            ResultCode.SUCCESS,
            response.resultCode
        )

        assertEquals(
            expectedResponse,
            response.data
        )

        coVerify(exactly = 1) {
            workflowPort.processPayment(request)
        }
    }

    @Test
    suspend fun `Feature - Create payment Scenario - Workflow is invoked exactly once`() {

        val request = validRequest()

        coEvery {
            workflowPort.processPayment(request)
        } returns CreatePaymentResponse(
            paymentReference =
                "11111111-1111-1111-1111-111111111111",
            feeReference = null
        )

        paymentService.createPayment(request)

        coVerify(exactly = 1) {
            workflowPort.processPayment(request)
        }
    }

    @Test
    suspend fun `Feature - Payment workflow failures Scenario - Runtime exception is propagated`() {

        val request = validRequest()

        coEvery {
            workflowPort.processPayment(request)
        } throws RuntimeException(
            "Workflow unavailable"
        )

        val exception =
            assertFailsWith<RuntimeException> {
                paymentService.createPayment(request)
            }

        assertEquals(
            "Workflow unavailable",
            exception.message
        )

        coVerify(exactly = 1) {
            workflowPort.processPayment(request)
        }
    }
}