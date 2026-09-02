package com.ojsolutions.infrastructure.database.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.datetime

object AccountTable : Table("account") {

    val id = uuid("id")
    val createdDate = datetime("created_date")
    val updatedDate = datetime("updated_date")
    val ownerId = uuid("owner_id")
    val ownerCategory = varchar("owner_category", 50)
    val status = varchar("status", 20)
    val ledger = varchar("ledger", 20)
    val accountNumber = varchar("account_number", 50).uniqueIndex()
    val accountType = varchar("account_type", 30)
    val description = varchar("description", 255)

    override val primaryKey = PrimaryKey(id)
}