package com.ojsolutions.api

import com.ojsolutions.module
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PaymentRouteValidationTest {

    @Test
    fun `Feature - Payment date validation Scenario - Invalid from date returns bad request`() =
        testApplication {

            application {
                module(
                    initialiseInfrastructure = false,
                    startWorkflowServer = false
                )
            }

            val response = client.get("/payments?from=not-a-date")

            assertEquals(
                HttpStatusCode.BadRequest,
                response.status
            )

            assertTrue(
                response.bodyAsText().contains("INVALID_FROM_DATE")
            )
        }

    @Test
    fun `Feature - Payment date validation Scenario - Invalid to date returns bad request`() =
        testApplication {

            application {
                module(
                    initialiseInfrastructure = false,
                    startWorkflowServer = false
                )
            }

            val response = client.get("/payments?to=not-a-date")

            assertEquals(
                HttpStatusCode.BadRequest,
                response.status
            )

            assertTrue(
                response.bodyAsText().contains("INVALID_TO_DATE")
            )
        }

    @Test
    fun `Feature - Payment date validation Scenario - From date after to date returns bad request`() =
        testApplication {

            application {
                module(
                    initialiseInfrastructure = false,
                    startWorkflowServer = false
                )
            }

            val response = client.get(
                "/payments" +
                        "?from=2026-08-26T10:00:00" +
                        "&to=2026-08-25T10:00:00"
            )

            assertEquals(
                HttpStatusCode.BadRequest,
                response.status
            )

            assertTrue(
                response.bodyAsText().contains("INVALID_DATE_RANGE")
            )
        }

    @Test
    fun `Feature - Payment reference validation Scenario - Invalid payment reference returns bad request`() =
        testApplication {

            application {
                module(
                    initialiseInfrastructure = false,
                    startWorkflowServer = false
                )
            }

            val response = client.get("/payments/not-a-uuid")

            assertEquals(
                HttpStatusCode.BadRequest,
                response.status
            )

            assertTrue(
                response.bodyAsText().contains("INVALID_PAYMENT_REFERENCE")
            )
        }
}