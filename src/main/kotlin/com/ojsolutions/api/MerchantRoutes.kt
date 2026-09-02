package com.ojsolutions.api

import com.ojsolutions.api.request.CreateMerchantRequest
import com.ojsolutions.api.request.UpdateMerchantRequest
import com.ojsolutions.api.response.InvalidRequestException
import com.ojsolutions.application.MerchantService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.UUID
import kotlin.getValue

fun Route.merchantRoutes() {

    val merchantService by inject<MerchantService>()

    route("/merchants") {

        /**
         * Create a merchant.
         *
         * Summary: Create a merchant
         * Description: Creates a merchant - the merchant entity exists independently of the accounts (one to many).
         * Tags: Merchants
         */
        post {

            val request = call.receive<CreateMerchantRequest>()

            val response = merchantService.createMerchant(request)

            call.respond(HttpStatusCode.Created, response)
        }

        /**
         * Get all merchants.
         *
         * Summary: Get all merchants
         * Tags: Merchants
         */
        get {

            val response = merchantService.getMerchants()

            call.respond(HttpStatusCode.OK, response)
        }

        /**
         * Get a merchant using the merchant id.
         *
         * Summary: Get a merchant using merchant id
         * Tags: Merchants
         */
        get("/{id}") {

            val id = try {
                UUID.fromString(call.parameters["id"])
            } catch (ex: IllegalArgumentException) {
                throw InvalidRequestException(
                    "INVALID_MERCHANT_ID",
                    "Merchant ID must be a valid UUID."
                )
            }

            val response = merchantService.getMerchant(id)

            call.respond(HttpStatusCode.OK, response)
        }

        /**
         * Update a merchant using the merchant id.
         *
         * Summary: Update a merchant using merchant id
         * Tags: Merchants
         */
        put("/{id}") {

            val id = try {
                UUID.fromString(call.parameters["id"])
            } catch (ex: IllegalArgumentException) {
                throw InvalidRequestException(
                    "INVALID_MERCHANT_ID",
                    "Merchant ID must be a valid UUID."
                )
            }

            val request = call.receive<UpdateMerchantRequest>()

            val response = merchantService.updateMerchant(
                id = id,
                request = request
            )

            call.respond(HttpStatusCode.OK, response)
        }
    }
}