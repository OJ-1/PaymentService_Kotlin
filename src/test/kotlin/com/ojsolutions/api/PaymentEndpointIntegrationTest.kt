package com.ojsolutions.api

import com.ojsolutions.domain.PaymentType
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PaymentEndpointIntegrationTest {

    private val baseUrl = System.getenv("PAYMENT_SERVICE_URL") ?: "http://localhost:8080"

    private val client = HttpClient(CIO)

    private val jsonParser = Json { ignoreUnknownKeys = true }

    private fun findPaymentReference(body: String): String? {

        val root = jsonParser.parseToJsonElement(body).jsonObject

        val data = root["data"]?.jsonObject?: return null

        return data["paymentReference"]?.jsonPrimitive?.content
    }

    private suspend fun createPayment(
        msgId: String,
        debtorAccountNumber: String = "1001",
        creditorAccountNumber: String = "1002",
        amount: Long = 100,
        type: String = "MOBILE_TOP_UP"
    ) =
        client.post("$baseUrl/payments") {
            contentType(ContentType.Application.Json)

            setBody(
                """
            {
              "msgId": "$msgId",
              "debtorAccountNumber": "$debtorAccountNumber",
              "creditorAccountNumber": "$creditorAccountNumber",
              "amount": $amount,
              "asset": "ZAR",
              "assetType": "FIAT",
              "type": "$type"
            }
            """.trimIndent()
            )
        }


    @AfterAll
    fun tearDown() {
        client.close()
    }

    @Test
    fun `Feature - Payment endpoint Scenario - Get payments returns success`() = runBlocking {

        val response = client.get("$baseUrl/payments")

        val body = response.bodyAsText()

        assertEquals(
            HttpStatusCode.OK,
            response.status,
            body
        )

        assertTrue(
            body.contains("SUCCESS"),
            body
        )
    }

    @Test
    fun `Feature - Payment endpoint Scenario - Get payments with from date returns success`() =
        runBlocking {

            val response = client.get("$baseUrl/payments?from=2020-01-01T00:00:00")

            val body = response.bodyAsText()

            assertEquals(
                HttpStatusCode.OK,
                response.status,
                body
            )

            assertTrue(
                body.contains("SUCCESS"),
                body
            )
        }

    @Test
    fun `Feature - Payment endpoint Scenario - Get payments with to date returns success`() =
        runBlocking {

            val response = client.get("$baseUrl/payments?to=2100-01-01T00:00:00")

            val body = response.bodyAsText()

            assertEquals(
                HttpStatusCode.OK,
                response.status,
                body
            )

            assertTrue(
                body.contains("SUCCESS"),
                body
            )
        }

    @Test
    fun `Feature - Payment endpoint Scenario - Successful payment can be retrieved`() =
        runBlocking {

            val suffix = UUID.randomUUID().toString().replace("-", "").take(12)

            val createResponse = client.post("$baseUrl/payments") {
                contentType(ContentType.Application.Json)

                setBody(
                    """
                    {
                      "msgId": "payment-$suffix",
                      "paymentKey": "payment-$suffix",
                      "debtorAccountNumber": "1001",
                      "creditorAccountNumber": "1002",
                      "amount": 100,
                      "asset": "ZAR",
                      "assetType": "FIAT",
                      "type": "MOBILE_TOP_UP"
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

            assertTrue(
                createBody.contains("SUCCESS"),
                createBody
            )

            val paymentReference = findPaymentReference(createBody)

            assertNotNull(
                paymentReference,
                createBody
            )

            val getResponse = client.get("$baseUrl/payments/$paymentReference")

            val getBody = getResponse.bodyAsText()

            assertEquals(
                HttpStatusCode.OK,
                getResponse.status,
                getBody
            )

            assertTrue(
                getBody.contains("SUCCESS"),
                getBody
            )

            assertTrue(
                getBody.contains(paymentReference),
                getBody
            )
        }

    @Test
    fun `Feature - Payment validation Scenario - Negative amount returns bad request`() =
        runBlocking {

            val suffix = UUID.randomUUID().toString().replace("-", "").take(12)

            val response = createPayment(
                msgId = "negative-$suffix",
                amount = -100
            )

            val body = response.bodyAsText()

            assertEquals(
                HttpStatusCode.BadRequest,
                response.status,
                body
            )

            assertTrue(
                body.contains("INVALID_TRANSACTION_AMOUNT"),
                body
            )
        }

    @Test
    fun `Feature - Payment validation Scenario - Same debtor and creditor returns bad request`() =
        runBlocking {

            val suffix = UUID.randomUUID().toString().replace("-", "").take(12)

            val response = createPayment(
                msgId = "same-account-$suffix",
                debtorAccountNumber = "1001",
                creditorAccountNumber = "1001"
            )

            val body = response.bodyAsText()

            assertEquals(
                HttpStatusCode.BadRequest,
                response.status,
                body
            )

            assertTrue(
                body.contains("INVALID_ACCOUNTS"),
                body
            )
        }

    @Test
    fun `Feature - Payment validation Scenario - Invalid payment type returns bad request`() =
        runBlocking {

            val suffix = UUID.randomUUID().toString().replace("-", "").take(12)

            val response = createPayment(
                msgId = "invalid-type-$suffix",
                type = "NOT_A_REAL_PAYMENT_TYPE"
            )

            val body = response.bodyAsText()

            assertEquals(
                HttpStatusCode.BadRequest,
                response.status,
                body
            )

            assertTrue(
                body.contains("INVALID_PAYMENT_TYPE"),
                body
            )
        }

    @Test
    fun `Feature - Payment validation Scenario - Missing required field returns bad request`() =
        runBlocking {

            val suffix = UUID.randomUUID().toString().replace("-", "").take(12)

            val response = client.post("$baseUrl/payments") {
                contentType(ContentType.Application.Json)

                setBody(
                    """
                {
                  "msgId": "missing-field-$suffix",
                  "paymentKey": "missing-field-$suffix",
                  "debtorAccountNumber": "1001",
                  "amount": 100,
                  "asset": "ZAR",
                  "assetType": "FIAT",
                  "type": "MOBILE_TOP_UP"
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
    fun `Feature - Payment business failure Scenario - Missing debtor account returns bad request`() =
        runBlocking {

            val suffix = UUID.randomUUID().toString().replace("-", "").take(12)

            val response = createPayment(
                msgId = "missing-debtor-$suffix",
                debtorAccountNumber = "999999999999",
                creditorAccountNumber = "1002",
                amount = 100
            )

            val body = response.bodyAsText()

            assertEquals(
                HttpStatusCode.BadRequest,
                response.status,
                body
            )

            assertTrue(
                body.contains("DEBTOR_ACCOUNT_NOT_FOUND"),
                body
            )
        }

    @Test
    fun `Feature - Payment business failure Scenario - Missing creditor account returns bad request`() =
        runBlocking {

            val suffix = UUID.randomUUID().toString().replace("-", "").take(12)

            val response = createPayment(
                msgId = "missing-creditor-$suffix",
                debtorAccountNumber = "1001",
                creditorAccountNumber = "999999999999",
                amount = 100
            )

            val body = response.bodyAsText()

            assertEquals(
                HttpStatusCode.BadRequest,
                response.status,
                body
            )

            assertTrue(
                body.contains("CREDITOR_ACCOUNT_NOT_FOUND"),
                body
            )
        }

    @Test
    fun `Feature - Payment business failure Scenario - Insufficient funds returns failure`() = runBlocking {

            val suffix = UUID.randomUUID().toString().replace("-", "").take(12)

            val response = createPayment(
                msgId = "insufficient-$suffix",
                debtorAccountNumber = "1001",
                creditorAccountNumber = "1002",
                amount = 99999999_99
            )

            val body = response.bodyAsText()

            assertTrue(
                response.status == HttpStatusCode.BadRequest || response.status == HttpStatusCode.OK,
                body
            )

            assertTrue(
                body.contains("FAILED"),
                body
            )

            assertTrue(
                body.contains("INSUFFICIENT_FUNDS"),
                body
            )
        }

    @Test
    fun `Feature - Payment idempotency Scenario - Duplicate message id is rejected`() =
        runBlocking {

            val suffix = UUID.randomUUID().toString().replace("-", "").take(12)

            val msgId = "idempotent-$suffix"

            val firstResponse = createPayment(msgId = msgId)

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

            val firstReference = findPaymentReference(firstBody)

            assertNotNull(
                firstReference,
                firstBody
            )

            val secondResponse = createPayment(msgId = msgId)

            val secondBody = secondResponse.bodyAsText()

            assertEquals(
                HttpStatusCode.BadRequest,
                secondResponse.status,
                secondBody
            )

            assertTrue(
                secondBody.contains("FAILED"),
                secondBody
            )

            assertTrue(
                secondBody.contains("DUPLICATE_MESSAGE_ID"),
                secondBody
            )

            assertTrue(
                secondBody.contains(
                    "A payment with this message ID has already been processed."
                ),
                secondBody
            )
        }
}