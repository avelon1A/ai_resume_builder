package com.airesumebuilder.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class GuestLoginRequest(
    val deviceId: String
)

data class AuthResponse(
    val token: String,
    val userId: String,
    val email: String,
    val name: String,
    val subscriptionTier: String
)

data class GenerateResumeDto(
    val name: String,
    val education: String,
    val experience: String,
    val skills: String,
    val projects: String = "",
    val certifications: String = "",
    val template: String = "modern"
)

data class GenerateResumeResponse(
    val resume: String,
    val resumeId: String
)

data class GenerateCoverLetterDto(
    val name: String,
    val jobRole: String,
    val company: String,
    val jobDescription: String
)

data class GenerateCoverLetterResponse(
    val coverLetter: String
)

data class AnalyzeResumeDto(
    val resumeText: String
)

data class AnalyzeResumeResponse(
    val suggestions: List<String>,
    val score: Int
)

data class SaveResumeDto(
    val title: String,
    val content: String,
    val template: String = "modern"
)

data class ResumeDto(
    val id: String,
    val userId: String,
    val title: String,
    val content: String,
    val template: String,
    val createdAt: String,
    val updatedAt: String
)

data class ResumeListResponse(
    val resumes: List<ResumeDto>
)
