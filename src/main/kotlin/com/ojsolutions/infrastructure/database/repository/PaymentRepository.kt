package com.ojsolutions.infrastructure.database.repository

import com.ojsolutions.domain.Asset
import com.ojsolutions.domain.AssetType
import com.ojsolutions.domain.PaymentType
import com.ojsolutions.domain.PaymentStatus
import com.ojsolutions.domain.Payment
import com.ojsolutions.infrastructure.database.table.PaymentTable
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDateTime
import java.util.UUID
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

class PaymentRepository {

    // Convert Kotlin Uuid to Java UUID
    private fun Uuid.toJavaUUID(): UUID = UUID.fromString(toString())

    // CREATE PAYMENT
    fun create(payment: Payment) {
        transaction {
            PaymentTable.insert {
                it[PaymentTable.id] = payment.id.toKotlinUuid()
                it[PaymentTable.status] = payment.status.name
                it[PaymentTable.paymentReference] = payment.paymentReference
                it[PaymentTable.feeReference] = payment.feeReference
                it[PaymentTable.debtorAccount] = payment.debtorAccount
                it[PaymentTable.creditorAccount] = payment.creditorAccount
                it[PaymentTable.feeAccount] = payment.feeAccount
                it[PaymentTable.amount] = payment.amount
                it[PaymentTable.asset] = payment.asset.name
                it[PaymentTable.assetType] = payment.assetType.name
                it[PaymentTable.paymentType] = payment.paymentType.name
                it[PaymentTable.feeRate] = payment.feeRate
                it[PaymentTable.feeAmount] = payment.feeAmount
                it[PaymentTable.createdDate] = payment.createdDate
                it[PaymentTable.updatedDate] = payment.updatedDate
            }
        }
    }

    // UPDATE PAYMENT STATUS
    fun updateStatus(paymentId: UUID, updatedStatus: PaymentStatus) = transaction {
        PaymentTable.update(
            {PaymentTable.id eq paymentId.toKotlinUuid()}
        ) {
            it[status] = updatedStatus.name
            it[updatedDate] = LocalDateTime.now()
        }
    }

    // GET PAYMENT
    fun get(paymentReference: String): Payment = transaction {
        PaymentTable.selectAll().where {
            PaymentTable.paymentReference eq paymentReference
        }
        .single()
        .let {
            Payment(
                id = it[PaymentTable.id].toJavaUUID(),
                createdDate = it[PaymentTable.createdDate],
                updatedDate = it[PaymentTable.updatedDate],
                status = PaymentStatus.valueOf(it[PaymentTable.status]),
                paymentReference = it[PaymentTable.paymentReference],
                feeReference = it[PaymentTable.feeReference],
                debtorAccount = it[PaymentTable.debtorAccount],
                creditorAccount = it[PaymentTable.creditorAccount],
                feeAccount = it[PaymentTable.feeAccount],
                amount = it[PaymentTable.amount],
                asset = Asset.valueOf(it[PaymentTable.asset]),
                assetType = AssetType.valueOf(it[PaymentTable.assetType]),
                paymentType = PaymentType.valueOf(it[PaymentTable.paymentType]),
                feeRate = it[PaymentTable.feeRate],
                feeAmount = it[PaymentTable.feeAmount]
            )
        }
    }

    // GET PAYMENTS (WITH OPTIONAL DATE RANGE)
    fun getAll(from: LocalDateTime? = null, to: LocalDateTime? = null): List<Payment> = transaction {

        var query = PaymentTable.selectAll()

        // Check if the FROM value is null - if not use it in the query
        if (from != null) {
            query = query.andWhere {
                PaymentTable.createdDate greaterEq from
            }
        }

        // Check if the TO value is null - if not use it in the query
        if (to != null) {
            query = query.andWhere {
                PaymentTable.createdDate lessEq to
            }
        }

        // Query the Payments table
        query
            .orderBy(PaymentTable.createdDate, SortOrder.DESC)
            .map {
                Payment(
                    id = it[PaymentTable.id].toJavaUUID(),
                    createdDate = it[PaymentTable.createdDate],
                    updatedDate = it[PaymentTable.updatedDate],
                    status = PaymentStatus.valueOf(it[PaymentTable.status]),
                    paymentReference = it[PaymentTable.paymentReference],
                    feeReference = it[PaymentTable.feeReference],
                    debtorAccount = it[PaymentTable.debtorAccount],
                    creditorAccount = it[PaymentTable.creditorAccount],
                    feeAccount = it[PaymentTable.feeAccount],
                    amount = it[PaymentTable.amount],
                    asset = Asset.valueOf(it[PaymentTable.asset]),
                    assetType = AssetType.valueOf(it[PaymentTable.assetType]),
                    paymentType = PaymentType.valueOf(it[PaymentTable.paymentType]),
                    feeRate = it[PaymentTable.feeRate],
                    feeAmount = it[PaymentTable.feeAmount]
                )
            }
    }

    // Check if the Id exists
    fun existsById(id: UUID): Boolean = transaction {
        PaymentTable.selectAll().where {
            PaymentTable.id eq id.toKotlinUuid()
        }.count() > 0
    }

    // Check if the PaymentReference exists
    fun existsByPaymentReference(paymentReference: String): Boolean = transaction {
        PaymentTable.selectAll().where {
            PaymentTable.paymentReference eq paymentReference
        }.count() > 0
    }

    // Check if the FeeReference exists
    fun existsByFeeReference(feeReference: String): Boolean = transaction {
        PaymentTable.selectAll().where {
            PaymentTable.feeReference eq feeReference
        }.count() > 0
    }

    //===
}

