package com.airesumebuilder.services

import com.airesumebuilder.ai.OpenAIService
import com.airesumebuilder.database.ResumesTable
import com.airesumebuilder.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*

class ResumeService(
    private val openAIService: OpenAIService,
    private val authService: AuthService
) {

    suspend fun generateResume(request: GenerateResumeRequest, userId: String): GenerateResumeResponse {
        // Rate limit temporarily disabled for testing
        // if (!authService.checkRateLimit(userId)) {
        //     throw IllegalStateException("Rate limit exceeded. Free users can make 100 AI requests per day.")
        // }

        val resumeContent = openAIService.generateResume(
            name = request.name,
            education = request.education,
            experience = request.experience,
            skills = request.skills,
            projects = request.projects,
            certifications = request.certifications,
            template = request.template
        )

        authService.incrementRequestCount(userId)

        val resumeId = saveResume(userId, request.name, resumeContent, request.template)

        return GenerateResumeResponse(resume = resumeContent, resumeId = resumeId)
    }

    suspend fun generateCoverLetter(request: GenerateCoverLetterRequest, userId: String): GenerateCoverLetterResponse {
        // Rate limit temporarily disabled for testing
        // if (!authService.checkRateLimit(userId)) {
        //     throw IllegalStateException("Rate limit exceeded. Free users can make 100 AI requests per day.")
        // }

        val coverLetter = openAIService.generateCoverLetter(
            name = request.name,
            jobRole = request.jobRole,
            company = request.company,
            jobDescription = request.jobDescription
        )

        authService.incrementRequestCount(userId)

        return GenerateCoverLetterResponse(coverLetter = coverLetter)
    }

    suspend fun analyzeResume(request: AnalyzeResumeRequest, userId: String): AnalyzeResumeResponse {
        // Rate limit temporarily disabled for testing
        // if (!authService.checkRateLimit(userId)) {
        //     throw IllegalStateException("Rate limit exceeded. Free users can make 100 AI requests per day.")
        // }

        val (score, suggestions) = openAIService.analyzeResume(request.resumeText)
        // authService.incrementRequestCount(userId)

        return AnalyzeResumeResponse(suggestions = suggestions, score = score)
    }

    fun saveResume(userId: String, title: String, content: String, template: String): String {
        return transaction {
            val resumeId = UUID.randomUUID().toString()
            ResumesTable.insert {
                it[ResumesTable.id] = resumeId
                it[ResumesTable.userId] = userId
                it[ResumesTable.title] = title
                it[ResumesTable.content] = content
                it[ResumesTable.template] = template
            }
            resumeId
        }
    }

    fun getUserResumes(userId: String): List<Resume> {
        return transaction {
            ResumesTable.selectAll().where { ResumesTable.userId eq userId }
                .orderBy(ResumesTable.createdAt, SortOrder.DESC)
                .map { row ->
                    Resume(
                        id = row[ResumesTable.id],
                        userId = row[ResumesTable.userId],
                        title = row[ResumesTable.title],
                        content = row[ResumesTable.content],
                        template = row[ResumesTable.template],
                        createdAt = row[ResumesTable.createdAt].toString(),
                        updatedAt = row[ResumesTable.updatedAt].toString()
                    )
                }
        }
    }

    fun getResume(resumeId: String, userId: String): Resume? {
        return transaction {
            ResumesTable.selectAll().where {
                (ResumesTable.id eq resumeId) and (ResumesTable.userId eq userId)
            }.singleOrNull()?.let { row ->
                Resume(
                    id = row[ResumesTable.id],
                    userId = row[ResumesTable.userId],
                    title = row[ResumesTable.title],
                    content = row[ResumesTable.content],
                    template = row[ResumesTable.template],
                    createdAt = row[ResumesTable.createdAt].toString(),
                    updatedAt = row[ResumesTable.updatedAt].toString()
                )
            }
        }
    }

    fun updateResume(resumeId: String, userId: String, request: SaveResumeRequest): Resume? {
        return transaction {
            val updated = ResumesTable.update({
                (ResumesTable.id eq resumeId) and (ResumesTable.userId eq userId)
            }) {
                it[ResumesTable.title] = request.title
                it[ResumesTable.content] = request.content
                it[ResumesTable.template] = request.template
                it[ResumesTable.updatedAt] = LocalDateTime.now()
            }

            if (updated > 0) {
                getResume(resumeId, userId)
            } else {
                null
            }
        }
    }

    fun deleteResume(resumeId: String, userId: String): Boolean {
        return transaction {
            ResumesTable.deleteWhere {
                (id eq resumeId) and (ResumesTable.userId eq userId)
            } > 0
        }
    }
}
