package com.ojsolutions.infrastructure.database

object DatabaseConfig {

    val HOST = System.getenv("DATABASE_HOST") ?: "localhost"
    val PORT = System.getenv("DATABASE_PORT") ?: "5433"

    const val DATABASE = "payments"

    const val USERNAME = "postgres"
    const val PASSWORD = "postgres"

    const val DRIVER = "org.postgresql.Driver"

    val JDBC_URL = "jdbc:postgresql://$HOST:$PORT/$DATABASE"
}