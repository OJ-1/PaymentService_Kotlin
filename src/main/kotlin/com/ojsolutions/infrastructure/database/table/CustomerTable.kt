package com.ojsolutions.infrastructure.database.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.datetime

object CustomerTable : Table("customer") {

    val id = uuid("id")
    val createdDate = datetime("created_date")
    val updatedDate = datetime("updated_date")
    val status = varchar("status", 50)
    val title = varchar("title", 20)
    val firstName = varchar("first_name", 100)
    val lastName = varchar("last_name", 100)
    val identityNumber = varchar("identity_number", 30).uniqueIndex()
    val passportNumber = varchar("passport_number", 30).nullable().uniqueIndex()
    val country = varchar("country", 255)
    val mobileNumber = varchar("mobile_number", 20)
    val email = varchar("email", 255).uniqueIndex()
    val physicalAddress = varchar("physical_address", 500)

    override val primaryKey = PrimaryKey(id)
}