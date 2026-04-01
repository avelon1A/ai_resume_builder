package com.airesumebuilder.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

object UsersTable : Table("users") {
    val id = varchar("id", 36)
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val name = varchar("name", 255)
    val subscriptionTier = varchar("subscription_tier", 50).default("free")
    val apiRequestsToday = integer("api_requests_today").default(0)
    val lastRequestDate = varchar("last_request_date", 10).default("")
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}

object ResumesTable : Table("resumes") {
    val id = varchar("id", 36)
    val userId = varchar("user_id", 36).references(UsersTable.id)
    val title = varchar("title", 255)
    val content = text("content")
    val template = varchar("template", 50).default("modern")
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}

object DatabaseFactory {
    fun init(config: ApplicationConfig) {
        val driver = config.propertyOrNull("database.driver")?.getString() ?: "org.h2.Driver"
        val jdbcUrl = config.propertyOrNull("database.jdbcURL")?.getString() ?: "jdbc:h2:mem:resumeBuilder;DB_CLOSE_DELAY=-1"
        val dbUser = config.propertyOrNull("database.user")?.getString() ?: ""
        val dbPass = config.propertyOrNull("database.password")?.getString() ?: ""

        val hikariConfig = HikariConfig().apply {
            driverClassName = driver
            this.jdbcUrl = jdbcUrl
            username = dbUser
            password = dbPass
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

        val dataSource = HikariDataSource(hikariConfig)
        Database.connect(dataSource)

        transaction {
            SchemaUtils.create(UsersTable, ResumesTable)
        }
    }
}
