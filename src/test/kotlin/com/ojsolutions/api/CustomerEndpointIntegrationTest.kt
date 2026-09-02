package com.ojsolutions.api

import com.ojsolutions.module
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CustomerEndpointIntegrationTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun findReference(body: String): String? = json.parseToJsonElement(body).jsonObject["data"]
        ?.jsonObject
        ?.get("reference")
        ?.jsonPrimitive
        ?.content

    @Test
    fun `Feature - Customer endpoint Scenario - Create customer returns created`() =
        testApplication {

            application {
                module(
                    initialiseInfrastructure = true,
                    startWorkflowServer = false
                )
            }

            val suffix = UUID.randomUUID().toString().replace("-", "").take(12)

            val response = client.post("/customers") {
                contentType(ContentType.Application.Json)
                setBody(
                    """
                {
                  "msgId": "customer-$suffix",
                  "title": "Mr",
                  "firstName": "John",
                  "lastName": "Test",
                  "identityNumber": "ID-$suffix",
                  "passportNumber": null,
                  "country": "ZA",
                  "mobileNumber": "+27110000000",
                  "email": "john-$suffix@example.com",
                  "physicalAddress": "1 Test Street"
                }
                """.trimIndent()
                )
            }

            val body = response.bodyAsText()

            assertEquals(
                HttpStatusCode.Created,
                response.status,
                body
            )

            assertTrue(body.contains("SUCCESS"))

            val customerId = json
                .parseToJsonElement(body)
                .jsonObject["data"]
                ?.jsonObject
                ?.values
                ?.firstOrNull()
                ?.jsonPrimitive
                ?.content

            assertTrue(!customerId.isNullOrBlank())
        }

    @Test
    fun `Feature - Customer endpoint Scenario - Get customers returns success`() =
        testApplication {

            application {
                module(
                    initialiseInfrastructure = true,
                    startWorkflowServer = false
                )
            }

            val response = client.get("/customers")

            assertEquals(
                HttpStatusCode.OK,
                response.status
            )

            assertTrue(
                response.bodyAsText().contains("SUCCESS")
            )
        }

    @Test
    fun `Feature - Customer endpoint Scenario - Invalid customer id returns bad request`() =
        testApplication {

            application {
                module(
                    initialiseInfrastructure = true,
                    startWorkflowServer = false
                )
            }

            val response = client.get("/customers/not-a-valid-uuid")

            assertEquals(
                HttpStatusCode.BadRequest,
                response.status
            )

            assertTrue(
                response.bodyAsText().contains("INVALID_CUSTOMER_ID")
            )
        }

    @Test
    fun `Feature - Customer endpoint Scenario - Created customer can be retrieved`() =
        testApplication {

            application {
                module(
                    initialiseInfrastructure = true,
                    startWorkflowServer = false
                )
            }

            val suffix = UUID.randomUUID().toString().replace("-", "").take(12)

            val createResponse = client.post("/customers") {
                contentType(ContentType.Application.Json)

                setBody(
                    """
                {
                  "msgId": "customer-$suffix",
                  "title": "Mr",
                  "firstName": "John",
                  "lastName": "RetrieveTest",
                  "identityNumber": "ID-$suffix",
                  "passportNumber": null,
                  "country": "ZA",
                  "mobileNumber": "+27110000000",
                  "email": "john-$suffix@example.com",
                  "physicalAddress": "1 Test Street"
                }
                """.trimIndent()
                )
            }

            val createBody = createResponse.bodyAsText()

            assertEquals(
                HttpStatusCode.Created,
                createResponse.status,
                createBody
            )

            val customerId = findReference(createBody)

            assertTrue(
                !customerId.isNullOrBlank(),
                createBody
            )

            val getResponse = client.get("/customers/$customerId")

            val getBody = getResponse.bodyAsText()

            assertEquals(
                HttpStatusCode.OK,
                getResponse.status,
                getBody
            )

            assertTrue(getBody.contains("SUCCESS"), getBody)
            assertTrue(getBody.contains(customerId), getBody)
            assertTrue(getBody.contains("RetrieveTest"), getBody)
        }

    @Test
    fun `Feature - Customer endpoint Scenario - Nonexistent customer returns not found`() =
        testApplication {

            application {
                module(
                    initialiseInfrastructure = true,
                    startWorkflowServer = false
                )
            }

            val customerId = UUID.randomUUID()

            val response = client.get("/customers/$customerId")

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
                body.contains("CUSTOMER_NOT_FOUND"),
                body
            )
        }

    @Test
    fun `Feature - Customer endpoint Scenario - Invalid create payload returns bad request`() =
        testApplication {

            application {
                module(
                    initialiseInfrastructure = true,
                    startWorkflowServer = false
                )
            }

            val response = client.post("/customers") {
                contentType(ContentType.Application.Json)

                setBody(
                    """
                {
                  "msgId": "invalid-customer",
                  "title": "Mr"
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

}