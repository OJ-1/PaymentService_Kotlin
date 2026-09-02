package com.ojsolutions.api

import com.ojsolutions.module
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccountEndpointIntegrationTest {

    @Test
    fun `Feature - Account endpoint Scenario - Get all accounts returns success`() =
        testApplication {

            application {
                module(
                    initialiseInfrastructure = true,
                    startWorkflowServer = false
                )
            }

            val response = client.get("/accounts")

            assertEquals(
                HttpStatusCode.OK,
                response.status
            )

            assertTrue(
                response.bodyAsText().contains("SUCCESS")
            )
        }

    @Test
    fun `Feature - Account endpoint Scenario - Get seeded account returns success`() =
        testApplication {

            application {
                module(
                    initialiseInfrastructure = true,
                    startWorkflowServer = false
                )
            }

            val response = client.get("/accounts/1001")

            assertEquals(
                HttpStatusCode.OK,
                response.status
            )

            val body = response.bodyAsText()

            assertTrue(body.contains("SUCCESS"))
            assertTrue(body.contains("1001"))
        }

    @Test
    fun `Feature - Account endpoint Scenario - Get account transfers returns success`() =
        testApplication {

            application {
                module(
                    initialiseInfrastructure = true,
                    startWorkflowServer = false
                )
            }

            val response = client.get("/accounts/1001/transfers")

            assertEquals(
                HttpStatusCode.OK,
                response.status
            )

            assertTrue(
                response.bodyAsText().contains("SUCCESS")
            )
        }

    @Test
    fun `Feature - Account endpoint Scenario - Update seeded account returns success`() =
        testApplication {

            application {
                module(
                    initialiseInfrastructure = true,
                    startWorkflowServer = false
                )
            }

            val response = client.put("/accounts/1002") {
                contentType(ContentType.Application.Json)
                setBody(
                    """
                    {
                      "msgId": "update-account-1002",
                      "status": "ACTIVE",
                      "accountType": "TRANSACTIONAL",
                      "description": "Updated by integration test"
                    }
                    """.trimIndent()
                )
            }

            assertEquals(
                HttpStatusCode.OK,
                response.status
            )

            assertTrue(
                response.bodyAsText().contains("SUCCESS")
            )
        }

    @Test
    fun `Feature - Account endpoint Scenario - Nonexistent account returns not found`() =
        testApplication {

            application {
                module(
                    initialiseInfrastructure = true,
                    startWorkflowServer = false
                )
            }

            val response = client.get("/accounts/999999999999")

            val body = response.bodyAsText()

            assertEquals(
                HttpStatusCode.OK,
                response.status,
                body
            )

            assertTrue(
                body.contains("FAILED"),
                body
            )

            assertTrue(
                body.contains("ACCOUNT_NOT_FOUND"),
                body
            )
        }

    @Test
    fun `Feature - Account endpoint Scenario - Invalid account update data returns bad request`() =
        testApplication {

            application {
                module(
                    initialiseInfrastructure = true,
                    startWorkflowServer = false
                )
            }

            val response = client.put("/accounts/1002") {
                contentType(ContentType.Application.Json)

                setBody(
                    """
                {
                  "msgId": "invalid-update-account-1002",
                  "status": "NOT_A_REAL_STATUS",
                  "accountType": "TRANSACTIONAL",
                  "description": "Invalid status test"
                }
                """.trimIndent()
                )
            }

            val body = response.bodyAsText()

            assertEquals(
                HttpStatusCode.BadRequest,
                response.status,
                body
            )

            assertTrue(
                body.contains("FAILED"),
                body
            )
        }

    @Test
    fun `Feature - Account endpoint Scenario - Update nonexistent account returns not found`() =
        testApplication {

            application {
                module(
                    initialiseInfrastructure = true,
                    startWorkflowServer = false
                )
            }

            val accountNumber = "999999999999"

            val response = client.put( "/accounts/$accountNumber") {
                contentType(ContentType.Application.Json)

                setBody(
                    """
                {
                  "msgId": "update-$accountNumber",
                  "status": "ACTIVE",
                  "accountType": "TRANSACTIONAL",
                  "description": "Should not exist"
                }
                """.trimIndent()
                )
            }

            val body = response.bodyAsText()

            assertEquals(
                HttpStatusCode.OK,
                response.status,
                body
            )

            assertTrue(
                body.contains("FAILED"),
                body
            )

            assertTrue(
                body.contains("ACCOUNT_NOT_FOUND"),
                body
            )

//            assertTrue(
//                body.contains("FAILED"),
//                body
//            )
        }

    @Test
    fun `Feature - Account endpoint Scenario - Account update persists changes`() =
        testApplication {

            application {
                module(
                    initialiseInfrastructure = true,
                    startWorkflowServer = false
                )
            }

            val accountNumber = "1002"
            val updatedDescription = "Updated integration test ${UUID.randomUUID()}"

            val updateResponse = client.put("/accounts/$accountNumber") {
                contentType(ContentType.Application.Json)

                setBody(
                    """
                {
                  "msgId": "update-$accountNumber",
                  "status": "ACTIVE",
                  "accountType": "TRANSACTIONAL",
                  "description": "$updatedDescription"
                }
                """.trimIndent()
                )
            }

            val updateBody = updateResponse.bodyAsText()

            assertEquals(
                HttpStatusCode.OK,
                updateResponse.status,
                updateBody
            )

            val getResponse = client.get("/accounts/$accountNumber")

            val getBody = getResponse.bodyAsText()

            assertEquals(
                HttpStatusCode.OK,
                getResponse.status,
                getBody
            )

            assertTrue(
                getBody.contains(updatedDescription),
                getBody
            )
        }
}