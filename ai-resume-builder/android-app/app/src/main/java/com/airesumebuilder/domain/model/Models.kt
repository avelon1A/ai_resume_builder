package com.airesumebuilder.domain.model

data class User(
    val id: String,
    val email: String,
    val name: String,
    val subscriptionTier: String = "free"
)

data class AuthState(
    val isAuthenticated: Boolean = false,
    val user: User? = null,
    val token: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class Resume(
    val id: String,
    val userId: String,
    val title: String,
    val content: String,
    val template: String,
    val createdAt: String,
    val updatedAt: String
)

data class GenerateResumeRequest(
    val name: String,
    val education: String,
    val experience: String,
    val skills: String,
    val projects: String = "",
    val certifications: String = "",
    val template: String = "modern"
)

data class GenerateCoverLetterRequest(
    val name: String,
    val jobRole: String,
    val company: String,
    val jobDescription: String
)

data class ResumeAnalysis(
    val suggestions: List<String>,
    val score: Int
)

sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()
    data object Idle : Resource<Nothing>()
}

enum class ResumeTemplate(val displayName: String) {
    MODERN("Modern"),
    MINIMAL("Minimal"),
    CLASSIC("Classic")
}
