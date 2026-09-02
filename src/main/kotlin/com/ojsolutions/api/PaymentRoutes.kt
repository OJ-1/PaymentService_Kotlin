package com.ojsolutions.api

import com.ojsolutions.api.request.CreatePaymentRequest
import com.ojsolutions.api.request.UpdatePaymentStatusRequest
import com.ojsolutions.api.response.ApiError
import com.ojsolutions.api.response.ApiResponse
import com.ojsolutions.api.response.InvalidRequestException
import com.ojsolutions.api.response.PaymentResponse
import com.ojsolutions.api.response.ResultCode
import com.ojsolutions.application.PaymentService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.time.LocalDateTime
import java.time.format.DateTimeParseException
import java.util.UUID

fun Route.paymentRoutes() {

    val paymentService by inject<PaymentService>()

    route("/payments") {

        /**
         * Create a payment.
         *
         * Summary: Create a payment
         * Description: Creates a payment between two accounts and applies the configured fee.
         * Tags: Payments
         */
        post {

            val request = call.receive<CreatePaymentRequest>()

            val response = paymentService.createPayment(request)

            call.respond(HttpStatusCode.Created, response)
        }

        /**
         * Get all payments.
         *
         * Summary: Get all payments with optional date range
         * Tags: Payments
         */
        get {

            val from = call.request.queryParameters["from"]?.let { value ->

                try {
                    LocalDateTime.parse(value)
                } catch (_: DateTimeParseException) {

                    throw InvalidRequestException(
                        code = "INVALID_FROM_DATE",
                        message = "Invalid from date."
                    )
                }
            }

            val to = call.request.queryParameters["to"]?.let { value ->

                try {
                    LocalDateTime.parse(value)
                } catch (_: DateTimeParseException) {

                    throw InvalidRequestException(
                        code = "INVALID_TO_DATE",
                        message = "Invalid to date."
                    )
                }
            }

            if (from != null && to != null && from.isAfter(to)) {
                throw InvalidRequestException(
                    "INVALID_DATE_RANGE",
                    "FROM date must not be after TO date"
                )
            }

            val response = paymentService.getPayments(from, to)

            call.respond(HttpStatusCode.OK, response)
        }

        /**
         * Get a payment using the payment reference.
         *
         * Summary: Get a payment using reference
         * Tags: Payments
         */
        get("/{paymentReference}") {

            val paymentReference = call.parameters["paymentReference"]

            val id = try {
                UUID.fromString(paymentReference)
            } catch (ex: IllegalArgumentException) {
                throw InvalidRequestException(
                    "INVALID_PAYMENT_REFERENCE",
                    "Payment Reference must be a valid UUID."
                )
            }

            if (paymentReference.isNullOrBlank()) {
                // 400
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<PaymentResponse>(
                        resultCode = ResultCode.FAILED,
                        data = null,
                        errors = listOf(
                            ApiError(
                                code = "INVALID_PAYMENT_REFERENCE",
                                description = "Payment reference is required."
                            )
                        )
                    )
                )

                return@get
            }

            val response = paymentService.getPayment(paymentReference)

            call.respond(HttpStatusCode.OK, response)
        }

        /**
         * Update a payment status using the payment id.
         *
         * Summary: Update a payment using payment id
         * Tags: Payments
         */
        put("/{id}") {

            val id = try {
                UUID.fromString(call.parameters["id"])
            } catch (ex: IllegalArgumentException) {
                throw InvalidRequestException(
                    "INVALID_PAYMENT_ID",
                    "Payment ID must be a valid UUID."
                )
            }

            val request = call.receive<UpdatePaymentStatusRequest>()

            val response = paymentService.updatePayment(
                id = id,
                request = request
            )

            call.respond(HttpStatusCode.OK, response)
        }
    }
}