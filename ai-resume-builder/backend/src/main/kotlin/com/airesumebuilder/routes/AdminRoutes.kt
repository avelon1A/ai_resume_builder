package com.airesumebuilder.routes

import com.airesumebuilder.database.ResumesTable
import com.airesumebuilder.database.UsersTable
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun Route.adminRoutes() {
    route("/admin") {

        get("/stats") {
            try {
                val stats = transaction {
                    val totalUsers = UsersTable.selectAll().count()
                    val totalResumes = ResumesTable.selectAll().count()
                    val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
                    val activeToday = UsersTable.selectAll()
                        .where { UsersTable.lastRequestDate eq today }
                        .count()
                    val premiumUsers = UsersTable.selectAll()
                        .where { UsersTable.subscriptionTier eq "premium" }
                        .count()
                    val freeUsers = totalUsers - premiumUsers
                    val guestUsers = UsersTable.selectAll()
                        .where { UsersTable.email like "%@device.local" }
                        .count()

                    mapOf(
                        "totalUsers" to totalUsers,
                        "totalResumes" to totalResumes,
                        "activeToday" to activeToday,
                        "premiumUsers" to premiumUsers,
                        "freeUsers" to freeUsers,
                        "guestUsers" to guestUsers
                    )
                }
                call.respond(HttpStatusCode.OK, stats)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to fetch stats"))
            }
        }

        get("/users") {
            try {
                val users = transaction {
                    UsersTable.selectAll().map { row ->
                        mapOf(
                            "id" to row[UsersTable.id],
                            "email" to row[UsersTable.email],
                            "name" to row[UsersTable.name],
                            "subscriptionTier" to row[UsersTable.subscriptionTier],
                            "apiRequestsToday" to row[UsersTable.apiRequestsToday],
                            "lastRequestDate" to row[UsersTable.lastRequestDate],
                            "createdAt" to row[UsersTable.createdAt].toString()
                        )
                    }
                }
                call.respond(HttpStatusCode.OK, mapOf("users" to users))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to fetch users"))
            }
        }

        get("/resumes") {
            try {
                val resumes = transaction {
                    ResumesTable
                        .join(UsersTable, JoinType.LEFT, ResumesTable.userId, UsersTable.id)
                        .selectAll()
                        .map { row ->
                            mapOf(
                                "id" to row[ResumesTable.id],
                                "userId" to row[ResumesTable.userId],
                                "userName" to row[UsersTable.name],
                                "userEmail" to row[UsersTable.email],
                                "title" to row[ResumesTable.title],
                                "template" to row[ResumesTable.template],
                                "createdAt" to row[ResumesTable.createdAt].toString()
                            )
                        }
                }
                call.respond(HttpStatusCode.OK, mapOf("resumes" to resumes))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to fetch resumes"))
            }
        }

        delete("/users/{id}") {
            try {
                val userId = call.parameters["id"] ?: return@delete call.respond(
                    HttpStatusCode.BadRequest, mapOf("error" to "User ID required")
                )

                transaction {
                    ResumesTable.deleteWhere { ResumesTable.userId eq userId }
                    UsersTable.deleteWhere { UsersTable.id eq userId }
                }
                call.respond(HttpStatusCode.OK, mapOf("message" to "User deleted"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to delete user"))
            }
        }

        delete("/resumes/{id}") {
            try {
                val resumeId = call.parameters["id"] ?: return@delete call.respond(
                    HttpStatusCode.BadRequest, mapOf("error" to "Resume ID required")
                )

                transaction {
                    ResumesTable.deleteWhere { ResumesTable.id eq resumeId }
                }
                call.respond(HttpStatusCode.OK, mapOf("message" to "Resume deleted"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to delete resume"))
            }
        }

        post("/reset-requests/{userId}") {
            try {
                val userId = call.parameters["userId"] ?: return@post call.respond(
                    HttpStatusCode.BadRequest, mapOf("error" to "User ID required")
                )

                transaction {
                    UsersTable.update({ UsersTable.id eq userId }) {
                        it[UsersTable.apiRequestsToday] = 0
                        it[UsersTable.lastRequestDate] = ""
                    }
                }
                call.respond(HttpStatusCode.OK, mapOf("message" to "Request count reset"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to reset requests"))
            }
        }

        post("/reset-all-requests") {
            try {
                transaction {
                    UsersTable.update { it[UsersTable.apiRequestsToday] = 0 }
                }
                call.respond(HttpStatusCode.OK, mapOf("message" to "All request counts reset"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to reset requests"))
            }
        }

        delete("/resumes") {
            try {
                transaction {
                    ResumesTable.deleteAll()
                }
                call.respond(HttpStatusCode.OK, mapOf("message" to "All resumes deleted"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to delete resumes"))
            }
        }

        delete("/users") {
            try {
                transaction {
                    ResumesTable.deleteAll()
                    UsersTable.deleteAll()
                }
                call.respond(HttpStatusCode.OK, mapOf("message" to "All users and resumes deleted"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to delete users"))
            }
        }
    }
}
