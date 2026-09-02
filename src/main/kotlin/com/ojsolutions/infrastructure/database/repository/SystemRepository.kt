package com.ojsolutions.infrastructure.database.repository

import com.ojsolutions.domain.System
import com.ojsolutions.domain.OwnerStatus
import com.ojsolutions.infrastructure.database.table.SystemTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

class SystemRepository {

    // Convert Kotlin Uuid to Java UUID
    private fun Uuid.toJavaUUID(): UUID = UUID.fromString(toString())

    // CREATE SYSTEM
    fun create(system: System) = transaction {
        SystemTable.insert {
            it[SystemTable.id] = system.id.toKotlinUuid()
            it[SystemTable.status] = system.status.name
            it[SystemTable.name] = system.name
            it[SystemTable.description] = system.description
            it[SystemTable.createdDate] = system.createdDate
            it[SystemTable.updatedDate] = system.updatedDate
        }
    }

    // UPDATE SYSTEM
    fun update(system: System) = transaction {
        SystemTable.update(
            {SystemTable.id eq system.id.toKotlinUuid()}
        ) {
            it[status] = system.status.name
            it[name] = system.name
            it[description] = system.description
            it[updatedDate] = system.updatedDate
        }
    }

    // GET SYSTEM
    fun get(id: UUID): System = transaction {
        SystemTable.selectAll()
            .where { SystemTable.id eq id.toKotlinUuid() }
            .single()
            .let {
                System(
                    id = it[SystemTable.id].toJavaUUID(),
                    createdDate = it[SystemTable.createdDate],
                    updatedDate = it[SystemTable.updatedDate],
                    status = OwnerStatus.valueOf(it[SystemTable.status]),
                    name = it[SystemTable.name],
                    description = it[SystemTable.description]
                )
            }
    }

    // GET ALL SYSTEMS
    fun getAll(): List<System> = transaction {
        SystemTable.selectAll().map {
            System(
                id = it[SystemTable.id].toJavaUUID(),
                createdDate = it[SystemTable.createdDate],
                updatedDate = it[SystemTable.updatedDate],
                status = OwnerStatus.valueOf(it[SystemTable.status]),
                name = it[SystemTable.name],
                description = it[SystemTable.description]
            )
        }
    }

    // Check if the Id exists
    fun existsById(id: UUID): Boolean = transaction {
        SystemTable.selectAll().where {
            SystemTable.id eq id.toKotlinUuid()
        } .count() > 0
    }

    // Check if the Name exists
    fun existsByName(name: String): Boolean = transaction {
        SystemTable.selectAll().where {
            SystemTable.name eq name
        }.count() > 0
    }

    //===
}