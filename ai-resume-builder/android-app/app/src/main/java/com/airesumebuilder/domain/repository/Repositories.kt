package com.airesumebuilder.domain.repository

import com.airesumebuilder.domain.model.*
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): Resource<AuthState>
    suspend fun register(email: String, password: String, name: String): Resource<AuthState>
    suspend fun logout()
    fun isLoggedIn(): Flow<Boolean>
    fun getToken(): Flow<String?>
    fun getUserId(): Flow<String?>
    fun getUserName(): Flow<String?>
    fun getUserEmail(): Flow<String?>
    fun getUserTier(): Flow<String>
}

interface ResumeRepository {
    suspend fun generateResume(request: GenerateResumeRequest): Resource<GenerateResumeResponse>
    suspend fun generateCoverLetter(request: GenerateCoverLetterRequest): Resource<String>
    suspend fun analyzeResume(resumeText: String): Resource<ResumeAnalysis>
    suspend fun getResumes(): Resource<List<Resume>>
    suspend fun getResume(resumeId: String): Resource<Resume>
    suspend fun updateResume(resumeId: String, title: String, content: String, template: String): Resource<Resume>
    suspend fun deleteResume(resumeId: String): Resource<Unit>
}

data class GenerateResumeResponse(
    val resume: String,
    val resumeId: String
)
