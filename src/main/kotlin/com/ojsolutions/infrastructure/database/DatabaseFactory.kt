package com.ojsolutions.infrastructure.database

import com.ojsolutions.infrastructure.database.table.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object DatabaseFactory {

    private lateinit var dataSource: HikariDataSource

    fun initialise() {

        val config = HikariConfig().apply {

            jdbcUrl = DatabaseConfig.JDBC_URL
            driverClassName = DatabaseConfig.DRIVER

            username = DatabaseConfig.USERNAME
            password = DatabaseConfig.PASSWORD

            maximumPoolSize = 10
            minimumIdle = 2

            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"

            validate()
        }

        dataSource = HikariDataSource(config)

        Database.connect(dataSource)

        transaction {
            SchemaUtils.create(
                CustomerTable,
                MerchantTable,
                AccountTable,
                SystemTable,
                PaymentTable,
                FeeTypeTable
            )
        }

        println("Connected to PostgreSQL")
    }

    fun close() {
        if (::dataSource.isInitialized) {
            dataSource.close()
        }
    }
}