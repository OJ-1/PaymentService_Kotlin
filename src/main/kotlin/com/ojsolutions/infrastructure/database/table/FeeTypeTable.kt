package com.ojsolutions.infrastructure.database.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.datetime

object FeeTypeTable : Table("fee_type") {

    val id = uuid("id")
    val createdDate = datetime("created_date")
    val updatedDate = datetime("updated_date")
    val type = varchar("type", 50)
    val asset = varchar("asset", 10)
    val rate = varchar("rate", 10)
    val description = varchar("description", 255)

    // Must only ever be one fee configuration PaymentType + Asset
    init {
        uniqueIndex(type, asset)
    }

    override val primaryKey = PrimaryKey(id)
}