package com.ojsolutions.infrastructure.database.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.datetime

object MerchantTable : Table("merchant") {

    val id = uuid("id")
    val createdDate = datetime("created_date")
    val updatedDate = datetime("updated_date")
    val status = varchar("status", 50)
    val type = varchar("type", 50)
    val name = varchar("name", 255).uniqueIndex()
    val registrationNumber = varchar("registration_number", 100).uniqueIndex()
    val country = varchar("country", 255)
    val mobileNumber = varchar("mobile_number", 20)
    val email = varchar("email", 255).uniqueIndex()
    val physicalAddress = varchar("physical_address", 500)

    override val primaryKey = PrimaryKey(id)
}