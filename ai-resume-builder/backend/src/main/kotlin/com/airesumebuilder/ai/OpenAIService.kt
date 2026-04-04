package com.airesumebuilder.ai

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class OpenAIMessage(
    val role: String,
    val content: String
)

@Serializable
data class OpenAIRequest(
    val model: String = "gpt-4o-mini",
    val messages: List<OpenAIMessage>,
    val max_tokens: Int = 2000,
    val temperature: Double = 0.7
)

@Serializable
data class OpenAIChoice(
    val message: OpenAIMessage
)

@Serializable
data class OpenAIResponse(
    val choices: List<OpenAIChoice>
)

@Serializable
data class OpenAIError(
    val error: OpenAIErrorDetail
)

@Serializable
data class OpenAIErrorDetail(
    val message: String,
    val type: String,
    val code: String? = null
)

class OpenAIService(private val apiKey: String) {

    private val client = HttpClient(CIO) {
        install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    init {
        if (apiKey.isBlank()) {
            println("WARNING: OpenAI API key is not set! Set OPENAI_API_KEY environment variable.")
        }
    }

    suspend fun generateResume(
        name: String,
        education: String,
        experience: String,
        skills: String,
        projects: String,
        certifications: String,
        template: String
    ): String {
        val prompt = buildString {
            appendLine("Generate a professional ATS-friendly resume in $template style for the following candidate.")
            appendLine("Format it cleanly with clear section headers. Use bullet points for achievements.")
            appendLine()
            appendLine("Name: $name")
            appendLine("Education: $education")
            appendLine("Experience: $experience")
            appendLine("Skills: $skills")
            if (projects.isNotBlank()) appendLine("Projects: $projects")
            if (certifications.isNotBlank()) appendLine("Certifications: $certifications")
            appendLine()
            appendLine("Generate a complete, professional resume that can be exported to PDF.")
        }

        return callOpenAI(prompt)
    }

    suspend fun generateCoverLetter(
        name: String,
        jobRole: String,
        company: String,
        jobDescription: String
    ): String {
        val prompt = buildString {
            appendLine("Write a professional cover letter for the following application.")
            appendLine("Make it compelling, specific, and professional.")
            appendLine()
            appendLine("Candidate: $name")
            appendLine("Applying for: $jobRole at $company")
            appendLine("Job Description: $jobDescription")
            appendLine()
            appendLine("Generate a complete, professional cover letter.")
        }

        return callOpenAI(prompt)
    }

    suspend fun analyzeResume(resumeText: String): Pair<Int, List<String>> {
        val prompt = buildString {
            appendLine("Analyze this resume and provide improvement suggestions.")
            appendLine("Return a JSON object with: score (0-100) and suggestions (array of strings).")
            appendLine()
            appendLine("Resume:")
            appendLine(resumeText)
            appendLine()
            appendLine("Respond ONLY with valid JSON: {\"score\": <number>, \"suggestions\": [\"...\"]}")
        }

        val response = callOpenAI(prompt)
        return try {
            val json = Json { ignoreUnknownKeys = true }
            val parsed = json.decodeFromString<AnalysisResult>(response)
            Pair(parsed.score, parsed.suggestions)
        } catch (e: Exception) {
            Pair(70, listOf("Consider adding more quantifiable achievements", "Ensure consistent formatting"))
        }
    }

    private suspend fun callOpenAI(prompt: String): String {
        if (apiKey.isBlank()) {
            throw IllegalStateException("OpenAI API key is not set. Please set OPENAI_API_KEY environment variable.")
        }

        val request = OpenAIRequest(
            messages = listOf(
                OpenAIMessage(role = "system", content = "You are an expert resume writer and career coach. Provide professional, well-formatted responses."),
                OpenAIMessage(role = "user", content = prompt)
            )
        )

        try {
            val response = client.post("https://api.openai.com/v1/chat/completions") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $apiKey")
                setBody(request)
            }

            if (response.status != HttpStatusCode.OK) {
                try {
                    val errorBody = response.body<String>()
                    println("OpenAI API error: ${response.status} - $errorBody")
                } catch (e: Exception) {
                    println("OpenAI API error: ${response.status}")
                }
                throw IllegalStateException("OpenAI API error: ${response.status}. Check your API key.")
            }

            val openAIResponse: OpenAIResponse = response.body()
            return openAIResponse.choices.firstOrNull()?.message?.content ?: "Unable to generate response."
        } catch (e: Exception) {
            println("OpenAI request failed: ${e.message}")
            throw IllegalStateException("OpenAI request failed: ${e.message}")
        }
    }
}

@Serializable
private data class AnalysisResult(
    val score: Int,
    val suggestions: List<String>
)
