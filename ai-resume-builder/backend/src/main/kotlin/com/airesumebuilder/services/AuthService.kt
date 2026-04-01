package com.airesumebuilder.services

import at.favre.lib.crypto.bcrypt.BCrypt
import com.airesumebuilder.auth.JwtConfig
import com.airesumebuilder.database.UsersTable
import com.airesumebuilder.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class AuthService {

    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun register(request: RegisterRequest): AuthResponse {
        if (!emailRegex.matches(request.email)) {
            throw IllegalArgumentException("Invalid email format")
        }
        if (request.password.length < 6) {
            throw IllegalArgumentException("Password must be at least 6 characters")
        }
        if (request.name.isBlank()) {
            throw IllegalArgumentException("Name is required")
        }

        return transaction {
            val existing = UsersTable.select { UsersTable.email eq request.email }.singleOrNull()
            if (existing != null) {
                throw IllegalStateException("Email already registered")
            }

            val userId = UUID.randomUUID().toString()
            val hashedPassword = BCrypt.withDefaults().hashToString(12, request.password.toCharArray())

            UsersTable.insert {
                it[id] = userId
                it[email] = request.email
                it[passwordHash] = hashedPassword
                it[name] = request.name
                it[subscriptionTier] = "free"
            }

            val token = JwtConfig.generateToken(userId, request.email)
            AuthResponse(token, userId, request.email, request.name, "free")
        }
    }

    fun login(request: LoginRequest): AuthResponse {
        return transaction {
            val user = UsersTable.select { UsersTable.email eq request.email }.singleOrNull()
                ?: throw IllegalStateException("Invalid credentials")

            val hashedPassword = user[UsersTable.passwordHash]
            val result = BCrypt.verifyer().verify(request.password.toCharArray(), hashedPassword)
            if (!result.verified) {
                throw IllegalStateException("Invalid credentials")
            }

            val userId = user[UsersTable.id]
            val name = user[UsersTable.name]
            val tier = user[UsersTable.subscriptionTier]

            val token = JwtConfig.generateToken(userId, request.email)
            AuthResponse(token, userId, request.email, name, tier)
        }
    }

    fun checkRateLimit(userId: String): Boolean {
        return transaction {
            val user = UsersTable.select { UsersTable.id eq userId }.singleOrNull() ?: return@transaction false
            val tier = user[UsersTable.subscriptionTier]
            val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            val lastRequestDate = user[UsersTable.lastRequestDate]
            var requestsToday = user[UsersTable.apiRequestsToday]

            if (lastRequestDate != today) {
                requestsToday = 0
                UsersTable.update({ UsersTable.id eq userId }) {
                    it[apiRequestsToday] = 0
                    it[lastRequestDate] = today
                }
            }

            val limit = if (tier == "premium") Int.MAX_VALUE else 3
            requestsToday < limit
        }
    }

    fun incrementRequestCount(userId: String) {
        transaction {
            val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            val user = UsersTable.select { UsersTable.id eq userId }.singleOrNull() ?: return@transaction
            val lastRequestDate = user[UsersTable.lastRequestDate]
            var requestsToday = user[UsersTable.apiRequestsToday]

            if (lastRequestDate != today) {
                requestsToday = 0
            }

            UsersTable.update({ UsersTable.id eq userId }) {
                it[apiRequestsToday] = requestsToday + 1
                it[lastRequestDate] = today
            }
        }
    }

    fun getUserTier(userId: String): String {
        return transaction {
            UsersTable.select { UsersTable.id eq userId }
                .singleOrNull()?.get(UsersTable.subscriptionTier) ?: "free"
        }
    }
}
