package com.ojsolutions.infrastructure.database.repository

import com.ojsolutions.domain.Customer
import com.ojsolutions.domain.OwnerStatus
import com.ojsolutions.infrastructure.database.table.CustomerTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

class CustomerRepository {

    // Convert Kotlin Uuid to Java UUID
    private fun Uuid.toJavaUUID(): UUID = UUID.fromString(toString())

    // CREATE CUSTOMER
    fun create(customer: Customer) = transaction {
        CustomerTable.insert {
            it[CustomerTable.id] = customer.id.toKotlinUuid()
            it[CustomerTable.title] = customer.title
            it[CustomerTable.firstName] = customer.firstName
            it[CustomerTable.lastName] = customer.lastName
            it[CustomerTable.identityNumber] = customer.identityNumber
            it[CustomerTable.passportNumber] = customer.passportNumber
            it[CustomerTable.country] = customer.country
            it[CustomerTable.mobileNumber] = customer.mobileNumber
            it[CustomerTable.email] = customer.email
            it[CustomerTable.physicalAddress] = customer.physicalAddress
            it[CustomerTable.status] = customer.status.name
            it[CustomerTable.createdDate] = customer.createdDate
            it[CustomerTable.updatedDate] = customer.updatedDate
        }
    }

    // UPDATE CUSTOMER
    fun update(customer: Customer) = transaction {
        CustomerTable.update(
            {CustomerTable.id eq customer.id.toKotlinUuid()}
        ) {
            it[title] = customer.title
            it[firstName] = customer.firstName
            it[lastName] = customer.lastName
            it[identityNumber] = customer.identityNumber
            it[passportNumber] = customer.passportNumber
            it[country] = customer.country
            it[mobileNumber] = customer.mobileNumber
            it[email] = customer.email
            it[physicalAddress] = customer.physicalAddress
            it[status] = customer.status.name
            it[updatedDate] = customer.updatedDate
        }
    }

    // GET CUSTOMER
    fun get(id: UUID): Customer = transaction {
        CustomerTable.selectAll()
            .where { CustomerTable.id eq id.toKotlinUuid() }
            .single()
            .let {
                Customer(
                    id = it[CustomerTable.id].toJavaUUID(),
                    createdDate = it[CustomerTable.createdDate],
                    updatedDate = it[CustomerTable.updatedDate],
                    status = OwnerStatus.valueOf(it[CustomerTable.status]),
                    title = it[CustomerTable.title],
                    firstName = it[CustomerTable.firstName],
                    lastName = it[CustomerTable.lastName],
                    identityNumber = it[CustomerTable.identityNumber],
                    passportNumber = it[CustomerTable.passportNumber],
                    country = it[CustomerTable.country],
                    mobileNumber = it[CustomerTable.mobileNumber],
                    email = it[CustomerTable.email],
                    physicalAddress = it[CustomerTable.physicalAddress]
                )
            }
    }

    // GET ALL CUSTOMERS
    fun getAll(): List<Customer> = transaction {
        CustomerTable.selectAll()
            .map {
                Customer(
                    id = it[CustomerTable.id].toJavaUUID(),
                    createdDate = it[CustomerTable.createdDate],
                    updatedDate = it[CustomerTable.updatedDate],
                    status = OwnerStatus.valueOf(it[CustomerTable.status]),
                    title = it[CustomerTable.title],
                    firstName = it[CustomerTable.firstName],
                    lastName = it[CustomerTable.lastName],
                    identityNumber = it[CustomerTable.identityNumber],
                    passportNumber = it[CustomerTable.passportNumber],
                    country = it[CustomerTable.country],
                    mobileNumber = it[CustomerTable.mobileNumber],
                    email = it[CustomerTable.email],
                    physicalAddress = it[CustomerTable.physicalAddress]
                )
            }
    }

    // Check if the Id exists
    fun existsById(id: UUID): Boolean = transaction {
        CustomerTable.selectAll().where {
            CustomerTable.id eq id.toKotlinUuid()
        }.count() > 0
    }

    // Check if the ID Number exists
    fun existsByIdentityNumber(identityNumber: String): Boolean = transaction {
        CustomerTable.selectAll().where {
            CustomerTable.identityNumber eq identityNumber
        }.count() > 0
    }

    // Check if the Passport Number exists
    fun existsByPassportNumber(passportNumber: String): Boolean = transaction {
        CustomerTable.selectAll().where {
            CustomerTable.passportNumber eq passportNumber
        }.count() > 0
    }

    // Check if the Email exists
    fun existsByEmail(email: String): Boolean = transaction {
        CustomerTable.selectAll().where {
            CustomerTable.email eq email
        }.count() > 0
    }

    //====
}
