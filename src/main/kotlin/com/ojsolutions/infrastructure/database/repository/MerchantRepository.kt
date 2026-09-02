package com.ojsolutions.infrastructure.database.repository

import com.ojsolutions.domain.MerchantType
import com.ojsolutions.domain.Merchant
import com.ojsolutions.domain.OwnerStatus
import com.ojsolutions.infrastructure.database.table.MerchantTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

class MerchantRepository {

    // Convert Kotlin Uuid to Java UUID
    private fun Uuid.toJavaUUID(): UUID = UUID.fromString(toString())

    // CREATE MERCHANT
    fun create(merchant: Merchant) = transaction {
        MerchantTable.insert {
            it[MerchantTable.id] = merchant.id.toKotlinUuid()
            it[MerchantTable.type] = merchant.type.name
            it[MerchantTable.name] = merchant.name
            it[MerchantTable.registrationNumber] = merchant.registrationNumber
            it[MerchantTable.country] = merchant.country
            it[MerchantTable.mobileNumber] = merchant.mobileNumber
            it[MerchantTable.email] = merchant.email
            it[MerchantTable.physicalAddress] = merchant.physicalAddress
            it[MerchantTable.status] = merchant.status.name
            it[MerchantTable.createdDate] = merchant.createdDate
            it[MerchantTable.updatedDate] = merchant.updatedDate
        }
    }

    // UPDATE MERCHANT
    fun update(merchant: Merchant) = transaction {
        MerchantTable.update(
            {MerchantTable.id eq merchant.id.toKotlinUuid()}
        ) {
            it[status] = merchant.status.name
            it[type] = merchant.type.name
            it[name] = merchant.name
            it[registrationNumber] = merchant.registrationNumber
            it[country] = merchant.country
            it[mobileNumber] = merchant.mobileNumber
            it[email] = merchant.email
            it[physicalAddress] = merchant.physicalAddress
            it[updatedDate] = merchant.updatedDate
        }
    }

    // GET MERCHANT
    fun get(id: UUID): Merchant = transaction {
        MerchantTable.selectAll()
            .where { MerchantTable.id eq id.toKotlinUuid() }
            .single()
            .let {
                Merchant(
                    id = it[MerchantTable.id].toJavaUUID(),
                    createdDate = it[MerchantTable.createdDate],
                    updatedDate = it[MerchantTable.updatedDate],
                    status = OwnerStatus.valueOf(it[MerchantTable.status]),
                    type = MerchantType.valueOf(it[MerchantTable.type]),
                    name = it[MerchantTable.name],
                    registrationNumber = it[MerchantTable.registrationNumber],
                    country = it[MerchantTable.country],
                    mobileNumber = it[MerchantTable.mobileNumber],
                    email = it[MerchantTable.email],
                    physicalAddress = it[MerchantTable.physicalAddress]
                )
            }
    }

    // GET ALL MERCHANTS
    fun getAll(): List<Merchant> = transaction {
        MerchantTable.selectAll().map {
            Merchant(
                id = it[MerchantTable.id].toJavaUUID(),
                createdDate = it[MerchantTable.createdDate],
                updatedDate = it[MerchantTable.updatedDate],
                status = OwnerStatus.valueOf(it[MerchantTable.status]),
                type = MerchantType.valueOf(it[MerchantTable.type]),
                name = it[MerchantTable.name],
                registrationNumber = it[MerchantTable.registrationNumber],
                country = it[MerchantTable.country],
                mobileNumber = it[MerchantTable.mobileNumber],
                email = it[MerchantTable.email],
                physicalAddress = it[MerchantTable.physicalAddress]
            )
        }
    }

    // Check if the Id exists
    fun existsById(id: UUID): Boolean = transaction {
        MerchantTable.selectAll().where {
            MerchantTable.id eq id.toKotlinUuid()
        }.count() > 0
    }

    // Check if the Registration number exists
    fun existsByRegistrationNumber(registrationNumber: String): Boolean = transaction {
        MerchantTable.selectAll().where {
            MerchantTable.registrationNumber eq registrationNumber
        }.count() > 0
    }

    // Check if the Name exists
    fun existsByName(name: String): Boolean = transaction {
        MerchantTable.selectAll().where {
            MerchantTable.name eq name
        }.count() > 0
    }

    // Check if the Email exists
    fun existsByEmail(email: String): Boolean = transaction {
        MerchantTable.selectAll().where {
            MerchantTable.email eq email
        }.count() > 0
    }


    //===
}