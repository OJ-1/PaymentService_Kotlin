package com.ojsolutions.api

import com.ojsolutions.api.request.CreateAccountRequest
import com.ojsolutions.api.request.UpdateAccountRequest
import com.ojsolutions.api.response.AccountResponse
import com.ojsolutions.api.response.ApiError
import com.ojsolutions.api.response.ApiResponse
import com.ojsolutions.api.response.CreateReferenceResponse
import com.ojsolutions.api.response.ResultCode
import com.ojsolutions.api.response.TransfersResponse
import com.ojsolutions.application.AccountService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.accountRoutes() {

    val accountService by inject<AccountService>()

    route("/accounts") {

        /**
         * Create an account.
         *
         * Summary: Create an account
         * Description: Creates an account linked to a OwnerCategory (CUSTOMER/MERCHANT/SYSTEM).
         * Tags: Accounts
         */
        post {
            val request = call.receive<CreateAccountRequest>()

            val response = accountService.createAccount(request)

            call.respond(
                HttpStatusCode.Created,
                response
            )
        }

        /**
         * Get all accounts.
         *
         * Summary: Get all accounts
         * Tags: Accounts
         */
        get {
            val response = accountService.getAccounts()

            call.respond(
                HttpStatusCode.OK,
                response
            )
        }

        /**
         * Get an account using the account number.
         *
         * Summary: Get an account using account number
         * Tags: Accounts
         */
        get("/{accountNumber}") {

            val accountNumber = call.parameters["accountNumber"]

            if (accountNumber.isNullOrBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<AccountResponse>(
                        resultCode = ResultCode.FAILED,
                        errors = listOf(
                            ApiError(
                                code = "INVALID_ACCOUNT_NUMBER",
                                description = "Account number is required."
                            )
                        )
                    )
                )
                return@get
            }

            val response = accountService.getAccount(accountNumber)

            call.respond(
                HttpStatusCode.OK,
                response
            )
        }

        /**
         * Update an account using the account number.
         *
         * Summary: Update an account using account number
         * Tags: Accounts
         */
        put("/{accountNumber}") {

            val accountNumber = call.parameters["accountNumber"]

            if (accountNumber.isNullOrBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<CreateReferenceResponse>(
                        resultCode = ResultCode.FAILED,
                        errors = listOf(
                            ApiError(
                                code = "INVALID_ACCOUNT_NUMBER",
                                description = "Account number is required."
                            )
                        )
                    )
                )
                return@put
            }

            val request = call.receive<UpdateAccountRequest>()

            val response = accountService.updateAccount(
                accountNumber = accountNumber,
                request = request
            )

            call.respond(
                HttpStatusCode.OK,
                response
            )
        }

        /**
         * View transfers of an account using the account number.
         *
         * Summary: View transfers with optional date range
         * Description: View transfers of an account using account number - these are the actual ledger transfers of the account.
         * Tags: Transfers
         */
        get("/{accountNumber}/transfers") {

            val accountNumber = call.parameters["accountNumber"]

            if (accountNumber.isNullOrBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<TransfersResponse>(
                        resultCode = ResultCode.FAILED,
                        errors = listOf(
                            ApiError(
                                code = "INVALID_ACCOUNT_NUMBER",
                                description = "Account number is required."
                            )
                        )
                    )
                )
                return@get
            }

            val from = call.request.queryParameters["from"]?.toLongOrNull()
            val to = call.request.queryParameters["to"]?.toLongOrNull()

            val response = accountService.getTransfers(
                accountNumber = accountNumber,
                from = from,
                to = to
            )

            call.respond(
                HttpStatusCode.OK,
                response
            )
        }
    }
}