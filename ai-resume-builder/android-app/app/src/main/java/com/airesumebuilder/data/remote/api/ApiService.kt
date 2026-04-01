package com.airesumebuilder.data.remote.api

import com.airesumebuilder.data.remote.dto.*
import retrofit2.http.*

interface ApiService {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/guest")
    suspend fun guestLogin(@Body request: GuestLoginRequest): AuthResponse

    @POST("api/generate-resume")
    suspend fun generateResume(
        @Header("Authorization") token: String,
        @Body request: GenerateResumeDto
    ): GenerateResumeResponse

    @POST("api/generate-cover-letter")
    suspend fun generateCoverLetter(
        @Header("Authorization") token: String,
        @Body request: GenerateCoverLetterDto
    ): GenerateCoverLetterResponse

    @POST("api/analyze-resume")
    suspend fun analyzeResume(
        @Header("Authorization") token: String,
        @Body request: AnalyzeResumeDto
    ): AnalyzeResumeResponse

    @GET("api/resumes")
    suspend fun getResumes(
        @Header("Authorization") token: String
    ): ResumeListResponse

    @GET("api/resumes/{id}")
    suspend fun getResume(
        @Header("Authorization") token: String,
        @Path("id") resumeId: String
    ): ResumeDto

    @PUT("api/resumes/{id}")
    suspend fun updateResume(
        @Header("Authorization") token: String,
        @Path("id") resumeId: String,
        @Body request: SaveResumeDto
    ): ResumeDto

    @DELETE("api/resumes/{id}")
    suspend fun deleteResume(
        @Header("Authorization") token: String,
        @Path("id") resumeId: String
    ): Map<String, String>
}
