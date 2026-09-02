package com.ojsolutions.application

import com.ojsolutions.api.dto.PaymentDto
import com.ojsolutions.api.request.CreatePaymentRequest
import com.ojsolutions.api.request.UpdatePaymentStatusRequest
import com.ojsolutions.api.response.*
import com.ojsolutions.domain.Payment
import com.ojsolutions.domain.port.WorkflowPort
import com.ojsolutions.infrastructure.database.repository.PaymentRepository
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.time.LocalDateTime
import java.util.UUID

class PaymentService(
    private val workflowPort: WorkflowPort,
    private val paymentRepository: PaymentRepository
) {

    // CREATE PAYMENT
    suspend fun createPayment(request: CreatePaymentRequest): ApiResponse<CreatePaymentResponse> {

        val paymentResponse = workflowPort.processPayment(request)

        return ApiResponse(
            resultCode = ResultCode.SUCCESS,
            data = paymentResponse
        )
    }

    // UPDATE PAYMENT
    fun updatePayment(id: UUID, request: UpdatePaymentStatusRequest): ApiResponse<CreateReferenceResponse> {

        // Check if the paymentId exists
        if (!paymentRepository.existsById(id)) {
            return ApiResponse(
                resultCode = ResultCode.FAILED,
                errors = listOf(
                    ApiError(
                        code = "PAYMENT_NOT_FOUND",
                        description = "Payment does not exist."
                    )
                )
            )
        }

        // Update the payment status
        paymentRepository.updateStatus(id, request.status)

        return ApiResponse(
            resultCode = ResultCode.SUCCESS,
            data = CreateReferenceResponse(
                id.toString()
            )
        )
    }

    // GET ALL PAYMENTS
    fun getPayments(from: LocalDateTime? = null, to: LocalDateTime? = null): ApiResponse<PaymentsResponse> {

        // Validate the date range
        if (from != null && to != null && from.isAfter(to)) {
            return ApiResponse(
                resultCode = ResultCode.FAILED,
                errors = listOf(
                    ApiError(
                        code = "INVALID_DATE_RANGE",
                        description = "'from' must not be after 'to'."
                    )
                )
            )
        }

        // Get payments (using the specified date range if provided)
        val payments = paymentRepository.getAll(from, to)

        val response = payments.map { payment ->
            PaymentDto(
                id = payment.id.toString(),
                createdDate = payment.createdDate.toString(),
                updatedDate = payment.updatedDate.toString(),
                status = payment.status,
                paymentReference = payment.paymentReference,
                feeReference = payment.feeReference,
                debtorAccount = payment.debtorAccount,
                creditorAccount = payment.creditorAccount,
                feeAccount = payment.feeAccount,
                paymentType = payment.paymentType,
                amount = payment.amount,
                asset = payment.asset,
                assetType = payment.assetType,
                feeRate = payment.feeRate,
                feeAmount = payment.feeAmount
            )
        }

        return ApiResponse(
            resultCode = ResultCode.SUCCESS,
            data = PaymentsResponse(
                payments = response
            )
        )
    }

    // GET PAYMENT BY PAYMENT REFERENCE
    fun getPayment(id: String): ApiResponse<PaymentResponse> = transaction {

        // Check the paymentId exists
        if (!paymentRepository.existsByPaymentReference(id)) {
            return@transaction ApiResponse(
                resultCode = ResultCode.FAILED,
                errors = listOf(
                    ApiError(
                        code = "PAYMENT_NOT_FOUND",
                        description = "Payment does not exist."
                    )
                )
            )
        }

        // Retrieve the payment info from the DB
        val payment = paymentRepository.get(id)

        return@transaction ApiResponse(
            resultCode = ResultCode.SUCCESS,
            data = PaymentResponse(
                payment = PaymentDto(
                    id = payment.id.toString(),
                    createdDate = payment.createdDate.toString(),
                    updatedDate = payment.updatedDate.toString(),
                    status = payment.status,
                    paymentReference = payment.paymentReference,
                    feeReference = payment.feeReference,
                    debtorAccount = payment.debtorAccount,
                    creditorAccount = payment.creditorAccount,
                    feeAccount = payment.feeAccount,
                    paymentType = payment.paymentType,
                    amount = payment.amount,
                    asset = payment.asset,
                    assetType = payment.assetType,
                    feeRate = payment.feeRate,
                    feeAmount = payment.feeAmount
                )
            )
        )
    }
    
    
    //===
}
