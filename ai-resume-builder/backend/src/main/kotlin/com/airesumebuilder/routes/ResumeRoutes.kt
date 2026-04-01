package com.airesumebuilder.routes

import com.airesumebuilder.models.*
import com.airesumebuilder.services.ResumeService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.resumeRoutes(resumeService: ResumeService) {
    authenticate("auth-jwt") {
        route("/api") {
            post("/generate-resume") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.payload.getClaim("userId").asString()
                    val request = call.receive<GenerateResumeRequest>()
                    val response = resumeService.generateResume(request, userId)
                    call.respond(HttpStatusCode.OK, response)
                } catch (e: IllegalStateException) {
                    call.respond(HttpStatusCode.TooManyRequests, ErrorResponse(e.message ?: "Rate limit exceeded", 429))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Failed to generate resume"))
                }
            }

            post("/generate-cover-letter") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.payload.getClaim("userId").asString()
                    val request = call.receive<GenerateCoverLetterRequest>()
                    val response = resumeService.generateCoverLetter(request, userId)
                    call.respond(HttpStatusCode.OK, response)
                } catch (e: IllegalStateException) {
                    call.respond(HttpStatusCode.TooManyRequests, ErrorResponse(e.message ?: "Rate limit exceeded", 429))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Failed to generate cover letter"))
                }
            }

            post("/analyze-resume") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.payload.getClaim("userId").asString()
                    val request = call.receive<AnalyzeResumeRequest>()
                    val response = resumeService.analyzeResume(request, userId)
                    call.respond(HttpStatusCode.OK, response)
                } catch (e: IllegalStateException) {
                    call.respond(HttpStatusCode.TooManyRequests, ErrorResponse(e.message ?: "Rate limit exceeded", 429))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Failed to analyze resume"))
                }
            }

            get("/resumes") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.payload.getClaim("userId").asString()
                    val resumes = resumeService.getUserResumes(userId)
                    call.respond(HttpStatusCode.OK, ResumeListResponse(resumes))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Failed to fetch resumes"))
                }
            }

            get("/resumes/{id}") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.payload.getClaim("userId").asString()
                    val resumeId = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing resume ID"))
                    val resume = resumeService.getResume(resumeId, userId)
                    if (resume != null) {
                        call.respond(HttpStatusCode.OK, resume)
                    } else {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("Resume not found", 404))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Failed to fetch resume"))
                }
            }

            put("/resumes/{id}") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.payload.getClaim("userId").asString()
                    val resumeId = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing resume ID"))
                    val request = call.receive<SaveResumeRequest>()
                    val resume = resumeService.updateResume(resumeId, userId, request)
                    if (resume != null) {
                        call.respond(HttpStatusCode.OK, resume)
                    } else {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("Resume not found", 404))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Failed to update resume"))
                }
            }

            delete("/resumes/{id}") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.payload.getClaim("userId").asString()
                    val resumeId = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing resume ID"))
                    val deleted = resumeService.deleteResume(resumeId, userId)
                    if (deleted) {
                        call.respond(HttpStatusCode.OK, mapOf("message" to "Resume deleted"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("Resume not found", 404))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Failed to delete resume"))
                }
            }
        }
    }
}
