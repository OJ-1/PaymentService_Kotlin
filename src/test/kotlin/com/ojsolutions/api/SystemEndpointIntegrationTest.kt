package com.ojsolutions.api

import com.ojsolutions.module
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
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

class SystemEndpointIntegrationTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun findReference(body: String): String? = json.parseToJsonElement(body).jsonObject["data"]
        ?.jsonObject
        ?.get("reference")
        ?.jsonPrimitive
        ?.content

    @Test
    fun `Feature - System endpoint Scenario - Create system returns created`() =
        testApplication {

            application {
                module(
                    initialiseInfrastructure = true,
                    startWorkflowServer = false
                )
            }

            val suffix = UUID.randomUUID().toString()

            val response = client.post("/systems") {
                contentType(ContentType.Application.Json)
                setBody(
                    """
                    {
                      "msgId": "system-$suffix",
                      "name": "Test System $suffix",
                      "description": "Integration test system"
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
    fun `Feature - System endpoint Scenario - Get systems returns success`() =
        testApplication {

            application {
                module(
                    initialiseInfrastructure = true,
                    startWorkflowServer = false
                )
            }

            val response = client.get("/systems")

            assertEquals(
                HttpStatusCode.OK,
                response.status
            )

            assertTrue(
                response.bodyAsText().contains("SUCCESS")
            )
        }

    @Test
    fun `Feature - System endpoint Scenario - Invalid system id returns bad request`() =
        testApplication {

            application {
                module(
                    initialiseInfrastructure = true,
                    startWorkflowServer = false
                )
            }

            val response = client.get("/systems/not-a-valid-uuid")

            assertEquals(
                HttpStatusCode.BadRequest,
                response.status
            )

            assertTrue(
                response.bodyAsText().contains("INVALID_SYSTEM_ID")
            )
        }

    @Test
    fun `Feature - System endpoint Scenario - Created system can be retrieved`() =
        testApplication {

            application {
                module(
                    initialiseInfrastructure = true,
                    startWorkflowServer = false
                )
            }

            val suffix = UUID.randomUUID().toString().replace("-", "").take(12)

            val createResponse = client.post("/systems") {
                contentType(ContentType.Application.Json)

                setBody(
                    """
                {
                  "msgId": "system-$suffix",
                  "name": "Retrieve System $suffix",
                  "description": "System retrieval integration test"
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

            val systemId = findReference(createBody)

            assertTrue(
                !systemId.isNullOrBlank(),
                createBody
            )

            val getResponse = client.get("/systems/$systemId")

            val getBody = getResponse.bodyAsText()

            assertEquals(
                HttpStatusCode.OK,
                getResponse.status,
                getBody
            )

            assertTrue(getBody.contains("SUCCESS"), getBody)
            assertTrue(getBody.contains(systemId), getBody)
        }

    @Test
    fun `Feature - System endpoint Scenario - Nonexistent system returns not found`() =
        testApplication {

            application {
                module(
                    initialiseInfrastructure = true,
                    startWorkflowServer = false
                )
            }

            val response = client.get("/systems/99999999-9999-9999-9999-999999999999")

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
                body.contains("SYSTEM_NOT_FOUND"),
                body
            )
        }

    @Test
    fun `Feature - System endpoint Scenario - Invalid system creation data returns bad request`() =
        testApplication {

            application {
                module(
                    initialiseInfrastructure = true,
                    startWorkflowServer = false
                )
            }

            val response = client.post("/systems") {
                contentType(ContentType.Application.Json)

                setBody(
                    """
                {
                  "msgId": "invalid-system-create"
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
    fun `Feature - System endpoint Scenario - Duplicate message id behaviour is handled consistently`() =
        testApplication {

            application {
                module(
                    initialiseInfrastructure = true,
                    startWorkflowServer = false
                )
            }

            val suffix = UUID.randomUUID().toString().replace("-", "").take(12)

            val msgId = "duplicate-system-$suffix"
            val systemName = "Idempotent System $suffix"

            val firstResponse = client.post("/systems") {
                contentType(ContentType.Application.Json)

                setBody(
                    """
                {
                  "msgId": "$msgId",
                  "name": "$systemName",
                  "description": "Duplicate behaviour integration test"
                }
                """.trimIndent()
                )
            }

            val firstBody = firstResponse.bodyAsText()

            assertEquals(
                HttpStatusCode.Created,
                firstResponse.status,
                firstBody
            )

            assertTrue(
                firstBody.contains("SUCCESS"),
                firstBody
            )

            val firstSystemId = json.parseToJsonElement(firstBody).jsonObject["data"]
                ?.jsonObject
                ?.values
                ?.firstOrNull()
                ?.jsonPrimitive
                ?.content

            assertTrue(
                !firstSystemId.isNullOrBlank(),
                firstBody
            )

            val secondResponse = client.post("/systems") {
                contentType(ContentType.Application.Json)

                setBody(
                    """
                {
                  "msgId": "$msgId",
                  "name": "$systemName",
                  "description": "Duplicate behaviour integration test"
                }
                """.trimIndent()
                )
            }

            val secondBody = secondResponse.bodyAsText()

            assertTrue(
                secondResponse.status == HttpStatusCode.Created ||
                        secondResponse.status == HttpStatusCode.OK ||
                        secondBody.contains("FAILED"),
                secondBody
            )
        }

}