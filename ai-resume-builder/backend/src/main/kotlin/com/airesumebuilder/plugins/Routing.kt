package com.airesumebuilder.plugins

import com.airesumebuilder.ai.OpenAIService
import com.airesumebuilder.routes.adminRoutes
import com.airesumebuilder.routes.authRoutes
import com.airesumebuilder.routes.resumeRoutes
import com.airesumebuilder.services.AuthService
import com.airesumebuilder.services.ResumeService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val openAiApiKey = System.getenv("OPENAI_API_KEY") ?: environment.config.propertyOrNull("openai.apiKey")?.getString() ?: ""
    println("Initializing with OpenAI API key: ${if (openAiApiKey.isNotBlank()) "SET" else "NOT SET"}")
    val openAIService = OpenAIService(openAiApiKey)
    val authService = AuthService()
    val resumeService = ResumeService(openAIService, authService)

    routing {
        get("/") {
            call.respondText("AI Resume Builder API - v1.0", ContentType.Text.Plain)
        }

        get("/health") {
            call.respond(mapOf("status" to "healthy", "timestamp" to System.currentTimeMillis()))
        }

        authRoutes(authService)
        resumeRoutes(resumeService)
        adminRoutes()
    }
}
