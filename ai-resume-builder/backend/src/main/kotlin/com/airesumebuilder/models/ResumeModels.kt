package com.airesumebuilder.models

import kotlinx.serialization.Serializable

@Serializable
data class GenerateResumeRequest(
    val name: String,
    val education: String,
    val experience: String,
    val skills: String,
    val projects: String = "",
    val certifications: String = "",
    val template: String = "modern"
)

@Serializable
data class GenerateResumeResponse(
    val resume: String,
    val resumeId: String
)

@Serializable
data class GenerateCoverLetterRequest(
    val name: String,
    val jobRole: String,
    val company: String,
    val jobDescription: String
)

@Serializable
data class GenerateCoverLetterResponse(
    val coverLetter: String
)

@Serializable
data class AnalyzeResumeRequest(
    val resumeText: String
)

@Serializable
data class AnalyzeResumeResponse(
    val suggestions: List<String>,
    val score: Int
)

@Serializable
data class SaveResumeRequest(
    val title: String,
    val content: String,
    val template: String = "modern"
)

@Serializable
data class Resume(
    val id: String,
    val userId: String,
    val title: String,
    val content: String,
    val template: String,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class ResumeListResponse(
    val resumes: List<Resume>
)
