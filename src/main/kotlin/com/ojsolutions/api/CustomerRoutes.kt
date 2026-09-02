package com.ojsolutions.api

import com.ojsolutions.api.request.CreateCustomerRequest
import com.ojsolutions.api.request.UpdateCustomerRequest
import com.ojsolutions.api.response.InvalidRequestException
import com.ojsolutions.application.CustomerService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.UUID

fun Route.customerRoutes() {

    val customerService by inject<CustomerService>()

    route("/customers") {

        /**
         * Create a customer.
         *
         * Summary: Create a customer
         * Description: Creates a customer - the customer entity exists independently of the accounts (one to many).
         * Tags: Customers
         */
        post {

            val request = call.receive<CreateCustomerRequest>()

            val response = customerService.createCustomer(request)

            call.respond(HttpStatusCode.Created, response)
        }

        /**
         * Get all customers.
         *
         * Summary: Get all customers
         * Tags: Customers
         */
        get {

            val response = customerService.getCustomers()

            call.respond(HttpStatusCode.OK, response)
        }

        /**
         * Get a customer using the customer id.
         *
         * Summary: Get a customer using customer id
         * Tags: Customers
         */
        get("/{id}") {

            val id = try {
                UUID.fromString(call.parameters["id"])
            } catch (ex: IllegalArgumentException) {
                throw InvalidRequestException(
                    "INVALID_CUSTOMER_ID",
                    "Customer ID must be a valid UUID."
                )
            }

            val response = customerService.getCustomer(id)

            call.respond(HttpStatusCode.OK, response)
        }

        /**
         * Update a customer using the customer id.
         *
         * Summary: Update a customer using customer id
         * Tags: Customers
         */
        put("/{id}") {

            val id = try {
                UUID.fromString(call.parameters["id"])
            } catch (ex: IllegalArgumentException) {
                throw InvalidRequestException(
                    "INVALID_CUSTOMER_ID",
                    "Customer ID must be a valid UUID."
                )
            }

            val request = call.receive<UpdateCustomerRequest>()

            val response = customerService.updateCustomer(
                id = id,
                request = request
            )

            call.respond(HttpStatusCode.OK, response)
        }
    }
}