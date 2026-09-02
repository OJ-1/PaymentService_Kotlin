package com.ojsolutions.infrastructure.database.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.CurrentDateTime
import org.jetbrains.exposed.v1.javatime.datetime

object SystemTable : Table("system_account") {

    val id = uuid("id")
    val createdDate = datetime("created_date")
    val updatedDate = datetime("updated_date")
    val status = varchar("status", 50)
    val name = varchar("name", 50).uniqueIndex()
    val description = varchar("description", 255)

    override val primaryKey = PrimaryKey(id)
}