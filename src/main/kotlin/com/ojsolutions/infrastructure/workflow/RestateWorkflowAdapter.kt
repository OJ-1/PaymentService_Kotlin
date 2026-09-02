package com.ojsolutions.infrastructure.workflow

import com.ojsolutions.api.request.CreatePaymentRequest
import com.ojsolutions.api.response.CreatePaymentResponse
import com.ojsolutions.api.response.InvalidRequestException
import com.ojsolutions.api.response.PaymentTerminalException
import com.ojsolutions.domain.port.WorkflowPort
import dev.restate.client.Client
import dev.restate.client.IngressException
import dev.restate.client.kotlin.workflow

class RestateWorkflowAdapter(
    private val restateClient: Client
) : WorkflowPort {

    override suspend fun processPayment(request: CreatePaymentRequest): CreatePaymentResponse {

        // Use the msgId to confirm the same request has not already been received (currently this is persisted in Restate)
        val workflowId = "payment-${request.msgId}"

        return try {

            // Run the workflow
            restateClient
                .workflow<PaymentWorkflow>(workflowId)
                .run(request)

        } catch (ex: IngressException) {

            val message = ex.message.orEmpty()

            if (message.contains("workflow method was already invoked")) {
                throw InvalidRequestException(
                    code = "DUPLICATE_MESSAGE_ID",
                    message = "A payment with this message ID has already been processed."
                )
            }

            val error = message
                .substringAfter("] ", "")
                .substringBefore(". Got response body:")

            val code = error
                .substringBefore(":")
                .trim()

            val description = error
                .substringAfter(":")
                .trim()
                .trimEnd('.')

            throw PaymentTerminalException(
                code = code,
                message = description.ifBlank {
                    "Payment failed."
                }
            )
        }
    }
}