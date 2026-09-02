package com.ojsolutions.domain.port

import com.ojsolutions.api.request.CreatePaymentRequest
import com.ojsolutions.api.response.CreatePaymentResponse

interface WorkflowPort {

    suspend fun processPayment(request: CreatePaymentRequest): CreatePaymentResponse
}