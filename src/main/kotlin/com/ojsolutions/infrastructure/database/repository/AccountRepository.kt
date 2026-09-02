package com.ojsolutions.infrastructure.database.repository

import com.ojsolutions.domain.Account
import com.ojsolutions.domain.*
import com.ojsolutions.infrastructure.database.table.AccountTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

class AccountRepository {

    // Convert Kotlin Uuid to Java UUID
    private fun Uuid.toJavaUUID(): UUID = UUID.fromString(toString())

    // CREATE ACCOUNT
    fun create(account: Account) = transaction {
        AccountTable.insert {
            it[AccountTable.id] = account.id.toKotlinUuid()
            it[AccountTable.ownerId] = account.ownerId.toKotlinUuid()
            it[AccountTable.ownerCategory] = account.ownerCategory.name
            it[AccountTable.ledger] = account.ledger.name
            it[AccountTable.accountNumber] = account.accountNumber
            it[AccountTable.accountType] = account.accountType.name
            it[AccountTable.description] = account.description
            it[AccountTable.status] = account.status.name
            it[AccountTable.createdDate] = account.createdDate
            it[AccountTable.updatedDate] = account.updatedDate
        }
    }

    // UPDATE ACCOUNT
    fun update(account: Account) = transaction {
        AccountTable.update(
            {AccountTable.id eq account.id.toKotlinUuid()}
        ) {
            it[status] = account.status.name
            it[ledger] = account.ledger.name
            it[accountNumber] = account.accountNumber
            it[accountType] = account.accountType.name
            it[description] = account.description
            it[updatedDate] = account.updatedDate
        }
    }

    // GET ACCOUNT
    fun get(accountNumber: String): Account = transaction {
        AccountTable.selectAll().where { AccountTable.accountNumber eq accountNumber }.single().let {
            Account(
                id = it[AccountTable.id].toJavaUUID(),
                createdDate = it[AccountTable.createdDate],
                updatedDate = it[AccountTable.updatedDate],
                ownerId = it[AccountTable.ownerId].toJavaUUID(),
                ownerCategory = OwnerCategory.valueOf(it[AccountTable.ownerCategory]),
                status = AccountStatus.valueOf(it[AccountTable.status]),
                ledger = Ledger.valueOf(it[AccountTable.ledger]),
                accountNumber = it[AccountTable.accountNumber],
                accountType = AccountType.valueOf(it[AccountTable.accountType]),
                description = it[AccountTable.description]

            )
        }
    }

    // GET ALL ACCOUNTS
    fun getAll(): List<Account> = transaction {
        AccountTable.selectAll().map {
            Account(
                id = it[AccountTable.id].toJavaUUID(),
                createdDate = it[AccountTable.createdDate],
                updatedDate = it[AccountTable.updatedDate],
                ownerId = it[AccountTable.ownerId].toJavaUUID(),
                ownerCategory = OwnerCategory.valueOf(it[AccountTable.ownerCategory]),
                status = AccountStatus.valueOf(it[AccountTable.status]),
                ledger = Ledger.valueOf(it[AccountTable.ledger]),
                accountNumber = it[AccountTable.accountNumber],
                accountType = AccountType.valueOf(it[AccountTable.accountType]),
                description = it[AccountTable.description]
            )
        }
    }

    // Check if the Id exists
    fun existsById(id: UUID): Boolean = transaction {
        AccountTable.selectAll().where {
            AccountTable.id eq id.toKotlinUuid()
        }.count() > 0
    }

    // Check if the OwnerId exists
    fun existsByOwnerId(ownerId: UUID): Boolean = transaction {
        AccountTable.selectAll().where {
            AccountTable.ownerId eq ownerId.toKotlinUuid()
        }.count() > 0
    }

    // Check if the Account exists
    fun existsByAccountNumber(accountNumber: String): Boolean = transaction {
        AccountTable.selectAll().where {
            AccountTable.accountNumber eq accountNumber
        }.count() > 0
    }

    //===
}