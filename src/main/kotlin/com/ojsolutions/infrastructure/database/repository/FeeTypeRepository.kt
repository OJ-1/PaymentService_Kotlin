package com.ojsolutions.infrastructure.database.repository

import com.ojsolutions.api.response.FeeConfigurationNotFoundException
import com.ojsolutions.domain.FeeType
import com.ojsolutions.domain.Asset
import com.ojsolutions.domain.PaymentType
import com.ojsolutions.infrastructure.database.table.FeeTypeTable
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID
import kotlin.uuid.Uuid

class FeeTypeRepository {

    // Convert Kotlin Uuid to Java UUID
    private fun Uuid.toJavaUUID(): UUID = UUID.fromString(toString())

    // GET FEE COMBINATION
    fun get(type: PaymentType, asset: Asset): FeeType = transaction {

        val row = FeeTypeTable.selectAll()
            .where { (FeeTypeTable.type eq type.name) and (FeeTypeTable.asset eq asset.name) }
            .singleOrNull()
            ?: throw FeeConfigurationNotFoundException(
                "Fee configuration not found for payment type ${type.name} and asset ${asset.name}."
            )

        FeeType(
            id = row[FeeTypeTable.id].toJavaUUID(),
            createdDate = row[FeeTypeTable.createdDate],
            updatedDate = row[FeeTypeTable.updatedDate],
            paymentType = PaymentType.valueOf(row[FeeTypeTable.type]),
            asset = Asset.valueOf(row[FeeTypeTable.asset]),
            rate = row[FeeTypeTable.rate],
            description = row[FeeTypeTable.description]
        )
    }

    // GET ALL FEES
    fun getAll(): List<FeeType> = transaction {
        FeeTypeTable.selectAll().map {
            FeeType(
                id = it[FeeTypeTable.id].toJavaUUID(),
                createdDate = it[FeeTypeTable.createdDate],
                updatedDate = it[FeeTypeTable.updatedDate],
                paymentType = PaymentType.valueOf(it[FeeTypeTable.type]),
                asset = Asset.valueOf(it[FeeTypeTable.asset]),
                rate = it[FeeTypeTable.rate],
                description = it[FeeTypeTable.description]
            )
        }
    }

    //===
}