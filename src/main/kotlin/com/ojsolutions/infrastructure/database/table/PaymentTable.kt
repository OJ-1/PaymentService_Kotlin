package com.ojsolutions.infrastructure.database.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.datetime

object PaymentTable : Table("payment") {

    val id = uuid("id")
    val createdDate = datetime("created_date")
    val updatedDate = datetime("updated_date")
    val status = varchar("status", 20)
    val paymentReference = varchar("payment_reference", 100).uniqueIndex()
    val feeReference = varchar("fee_reference", 100).nullable().uniqueIndex()
    val debtorAccount = varchar("debtor_account", 50)
    val creditorAccount = varchar("creditor_account", 50)
    val feeAccount = varchar("fee_account", 50).nullable()
    val paymentType = varchar("payment_type", 50)
    val amount = long("amount")
    val asset = varchar("asset", 10)
    val assetType = varchar("asset_type", 20)
    val feeRate = varchar("fee_rate", 10)
    val feeAmount = long("fee_amount")

    override val primaryKey = PrimaryKey(id)
}