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

class MerchantEndpointIntegrationTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun findReference(body: String): String? = json.parseToJsonElement(body).jsonObject["data"]
        ?.jsonObject
        ?.get("reference")
        ?.jsonPrimitive
        ?.content

    @Test
    fun `Feature - Merchant endpoint Scenario - Create merchant returns created`() =
        testApplication {

            application {
                module(
                    initialiseInfrastructure = true,
                    startWorkflowServer = false
                )
            }

            val suffix = UUID.randomUUID().toString()

            val response = client.post("/merchants") {
                contentType(ContentType.Application.Json)
                setBody(
                    """
                    {
                      "msgId": "merchant-$suffix",
                      "type": "RETAIL",
                      "name": "Test Merchant $suffix",
                      "registrationNumber": "REG-$suffix",
                      "country": "ZA",
                      "mobileNumber": "+27110000001",
                      "email": "merchant-$suffix@example.com",
                      "physicalAddress": "2 Merchant Street"
                    }
                    """.trimIndent()
                )
            }

            assertEquals(
                HttpStatusCode.Created,
                response.status
            )

            assertTrue(
                response.bodyAsText().contains("SUCCESS")
            )
        }

    @Test
    fun `Feature - Merchant endpoint Scenario - Get merchants returns success`() =
        testApplication {

            application {
                module(
                    initialiseInfrastructure = true,
                    startWorkflowServer = false
                )
            }

            val response = client.get("/merchants")

            assertEquals(
                HttpStatusCode.OK,
                response.status
            )

            assertTrue(
                response.bodyAsText().contains("SUCCESS")
            )
        }

    @Test
    fun `Feature - Merchant endpoint Scenario - Invalid merchant id returns bad request`() =
        testApplication {

            application {
                module(
                    initialiseInfrastructure = true,
                    startWorkflowServer = false
                )
            }

            val response = client.get("/merchants/not-a-valid-uuid")

            assertEquals(
                HttpStatusCode.BadRequest,
                response.status
            )

            assertTrue(
                response.bodyAsText().contains("INVALID_MERCHANT_ID")
            )
        }

    @Test
    fun `Feature - Merchant endpoint Scenario - Created merchant can be retrieved`() =
        testApplication {

            application {
                module(
                    initialiseInfrastructure = true,
                    startWorkflowServer = false
                )
            }

            val suffix = UUID.randomUUID().toString().replace("-", "").take(12)

            val createResponse = client.post("/merchants") {
                contentType(ContentType.Application.Json)

                setBody(
                    """
                {
                  "msgId": "merchant-$suffix",
                  "type": "RETAIL",
                  "name": "Retrieve Merchant $suffix",
                  "registrationNumber": "REG-$suffix",
                  "country": "ZA",
                  "mobileNumber": "+27110000001",
                  "email": "merchant-$suffix@example.com",
                  "physicalAddress": "2 Merchant Street"
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

            val merchantId = findReference(createBody)

            assertTrue(
                !merchantId.isNullOrBlank(),
                createBody
            )

            val getResponse = client.get("/merchants/$merchantId")

            val getBody = getResponse.bodyAsText()

            assertEquals(
                HttpStatusCode.OK,
                getResponse.status,
                getBody
            )

            assertTrue(getBody.contains("SUCCESS"), getBody)
            assertTrue(getBody.contains(merchantId), getBody)
            assertTrue(
                getBody.contains("Retrieve Merchant"),
                getBody
            )
        }

    @Test
    fun `Feature - Merchant endpoint Scenario - Nonexistent merchant returns not found`() =
        testApplication {

            application {
                module(
                    initialiseInfrastructure = true,
                    startWorkflowServer = false
                )
            }

            val response = client.get("/merchants/${UUID.randomUUID()}")

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
                body.contains("MERCHANT_NOT_FOUND"),
                body
            )
        }

    @Test
    fun `Feature - Merchant endpoint Scenario - Invalid merchant type returns bad request`() =
        testApplication {

            application {
                module(
                    initialiseInfrastructure = true,
                    startWorkflowServer = false
                )
            }

            val suffix = UUID.randomUUID().toString().replace("-", "").take(12)

            val response = client.post("/merchants") {
                contentType(ContentType.Application.Json)

                setBody(
                    """
                {
                  "msgId": "merchant-$suffix",
                  "type": "INVALID_TYPE",
                  "name": "Invalid Merchant",
                  "registrationNumber": "REG-$suffix",
                  "country": "ZA",
                  "mobileNumber": "+27110000001",
                  "email": "merchant-$suffix@example.com",
                  "physicalAddress": "2 Merchant Street"
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