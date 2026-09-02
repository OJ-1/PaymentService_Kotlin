package com.ojsolutions

import com.ojsolutions.api.*
import com.ojsolutions.api.response.*
import com.ojsolutions.domain.ledger.*
import com.ojsolutions.infrastructure.ledger.LedgerSeeder
import com.ojsolutions.domain.port.LedgerPort
import com.ojsolutions.infrastructure.database.DatabaseFactory
import com.ojsolutions.infrastructure.database.DatabaseSeeder
import com.ojsolutions.infrastructure.di.appModule
import com.ojsolutions.infrastructure.workflow.startRestate
import io.ktor.http.HttpStatusCode
import io.ktor.openapi.OpenApiInfo
import org.koin.ktor.plugin.Koin
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.openapi.openAPI
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.response.respond
import io.ktor.server.routing.openapi.OpenApiDocSource
import io.ktor.server.routing.routing
import io.ktor.server.routing.routingRoot
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.get
import org.koin.ktor.ext.getKoin

fun main() {
    embeddedServer(
        factory = Netty,
        port = 8080,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module(initialiseInfrastructure: Boolean = true, startWorkflowServer: Boolean = true) {

    //========================================================= Koin \/

    install(Koin) {
        modules(appModule)
    }

    //========================================================= Koin /\

    // Control variable - controls whether the application starts the real infrastructure setup - route validation tests don't need it
    if (initialiseInfrastructure) {

        //========================================================= Database \/

        DatabaseFactory.initialise()
        DatabaseSeeder.seed()

        val ledgerPort: LedgerPort = get()

        LedgerSeeder.seed(ledgerPort)

        //========================================================= Database /\

        //========================================================= Restate \/

        // Control variable - Ktor route validation test a real workflow server
        if (startWorkflowServer) {
            startRestate(
                accountRepository = get(),
                feeTypeRepository = get(),
                ledgerPort = get(),
                paymentRepository = get()
            )
        }

        //========================================================= Restate /\
    }

    //========================================================= JSON \/

    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                ignoreUnknownKeys = true
                encodeDefaults = true
                explicitNulls = false
            }
        )
    }

    //========================================================= JSON /\

    //========================================================= Exception Handling \/

    install(StatusPages) {

        exception<CustomerAccountNotFoundException> { call, _ ->
            call.respond(
                HttpStatusCode.NotFound,
                ApiResponse<Unit>(
                    resultCode = ResultCode.FAILED,
                    errors = listOf(
                        ApiError(
                            code = "CUSTOMER_ACCOUNT_NOT_FOUND",
                            description = "Customer account not found."
                        )
                    )
                )
            )
        }

        exception<MerchantAccountNotFoundException> { call, _ ->
            call.respond(
                HttpStatusCode.NotFound,
                ApiResponse<Unit>(
                    resultCode = ResultCode.FAILED,
                    errors = listOf(
                        ApiError(
                            code = "MERCHANT_ACCOUNT_NOT_FOUND",
                            description = "Merchant account not found."
                        )
                    )
                )
            )
        }

        exception<TransferNotFoundException> { call, _ ->
            call.respond(
                HttpStatusCode.NotFound,
                ApiResponse<Unit>(
                    resultCode = ResultCode.FAILED,
                    errors = listOf(
                        ApiError(
                            code = "TRANSFER_NOT_FOUND",
                            description = "Transfer not found."
                        )
                    )
                )
            )
        }

        exception<InsufficientFundsException> { call, _ ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse<Unit>(
                    resultCode = ResultCode.FAILED,
                    errors = listOf(
                        ApiError(
                            code = "INSUFFICIENT_FUNDS",
                            description = "Insufficient funds."
                        )
                    )
                )
            )
        }

        exception<LedgerOperationException> { call, ex ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse<Unit>(
                    resultCode = ResultCode.FAILED,
                    errors = listOf(
                        ApiError(
                            code = "LEDGER_OPERATION_FAILED",
                            description = ex.message ?: "Ledger operation failed."
                        )
                    )
                )
            )
        }

        exception<BadRequestException> { call, ex ->

            val message = generateSequence(ex as Throwable?) { it.cause }
                .mapNotNull { it.message }
                .firstOrNull { it.contains("does not contain element with name") }

            val errorCode = when {
                message?.contains("$.ownerCategory") == true ->
                    "INVALID_OWNER_CATEGORY"

                message?.contains("$.ownerStatus") == true ->
                    "INVALID_OWNER_STATUS"

                message?.contains("$.accountStatus") == true ->
                    "INVALID_ACCOUNT_STATUS"

                message?.contains("$.accountType") == true ->
                    "INVALID_ACCOUNT_TYPE"

                message?.contains("$.merchantType") == true ->
                    "INVALID_MERCHANT_TYPE"

                message?.contains("$.asset") == true ->
                    "INVALID_ASSET"

                message?.contains("$.assetType") == true ->
                    "INVALID_ASSET_TYPE"

                message?.contains("$.type") == true ->
                    "INVALID_PAYMENT_TYPE"

                else ->
                    "INVALID_REQUEST"
            }

            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse<Unit>(
                    resultCode = ResultCode.FAILED,
                    errors = listOf(
                        ApiError(
                            code = errorCode,
                            description = message?.replace("com.ojsolutions.", "") ?: ex.message?.replace("com.ojsolutions.", "") ?: "Invalid request."
                        )
                    )
                )
            )
        }

        exception<PaymentTerminalException> { call, ex ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse<Unit>(
                    resultCode = ResultCode.FAILED,
                    errors = listOf(
                        ApiError(
                            code = ex.code,
                            description = ex.message
                        )
                    )
                )
            )
        }

        exception<InvalidRequestException> { call, ex ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse<Unit>(
                    resultCode = ResultCode.FAILED,
                    errors = listOf(
                        ApiError(
                            code = ex.code,
                            description = ex.message
                        )
                    )
                )
            )
        }

        exception<Throwable> { call, ex ->

//            ex.printStackTrace()

            this@module.environment.log.error(
                "Unhandled exception",
                ex
            )

            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse<Unit>(
                    resultCode = ResultCode.FAILED,
                    errors = listOf(
                        ApiError(
                            code = "UNEXPECTED_ERROR",
                            description = ex.message ?: "Unexpected server error."
                        )
                    )
                )
            )
        }
    }

    //========================================================= Exception Handling /\

    //========================================================= Routing \/

    routing {

        customerRoutes()
        merchantRoutes()
        systemRoutes()
        accountRoutes()
        paymentRoutes()

        openAPI(path = "openapi") {
            info = OpenApiInfo(
                title = "Payment Service API",
                version = "1.0.0",
                description = "Payment Service API using TigerBeetle"
            )

            source = OpenApiDocSource.Routing { routingRoot.descendants() }
        }

        swaggerUI(path = "swagger") {
            info = OpenApiInfo(
                title = "Payment Service API",
                version = "1.0.0",
                description = "Payment Service API using TigerBeetle"
            )

            source = OpenApiDocSource.Routing { routingRoot.descendants() }
        }
    }

    //========================================================= Routing /\

    //========================================================= Shutdown \/

    if (initialiseInfrastructure) {

        monitor.subscribe(ApplicationStopped) {

            val ledger = getKoin().get<LedgerPort>()

            if (ledger is AutoCloseable) {
                ledger.close()
            }

            DatabaseFactory.close()
        }
    }

    //========================================================= Shutdown /\

    //===
}