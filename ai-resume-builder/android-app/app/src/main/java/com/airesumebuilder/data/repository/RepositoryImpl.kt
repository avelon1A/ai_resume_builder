package com.airesumebuilder.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.airesumebuilder.data.remote.api.ApiService
import com.airesumebuilder.data.remote.dto.*
import com.airesumebuilder.domain.model.*
import com.airesumebuilder.domain.repository.AuthRepository
import com.airesumebuilder.domain.repository.ResumeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    @ApplicationContext private val context: Context
) : AuthRepository {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_TIER_KEY = stringPreferencesKey("user_tier")
        private val ONBOARDING_COMPLETE_KEY = booleanPreferencesKey("onboarding_complete")
    }

    override suspend fun login(email: String, password: String): Resource<AuthState> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            saveAuthData(response)
            Resource.Success(
                AuthState(
                    isAuthenticated = true,
                    user = User(response.userId, response.email, response.name, response.subscriptionTier),
                    token = response.token
                )
            )
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Login failed")
        }
    }

    override suspend fun register(email: String, password: String, name: String): Resource<AuthState> {
        return try {
            val response = apiService.register(RegisterRequest(email, password, name))
            saveAuthData(response)
            Resource.Success(
                AuthState(
                    isAuthenticated = true,
                    user = User(response.userId, response.email, response.name, response.subscriptionTier),
                    token = response.token
                )
            )
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Registration failed")
        }
    }

    override suspend fun logout() {
        context.dataStore.edit { it.clear() }
    }

    override fun isLoggedIn(): Flow<Boolean> = context.dataStore.data.map { it[TOKEN_KEY] != null }
    override fun getToken(): Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }
    override fun getUserId(): Flow<String?> = context.dataStore.data.map { it[USER_ID_KEY] }
    override fun getUserName(): Flow<String?> = context.dataStore.data.map { it[USER_NAME_KEY] }
    override fun getUserEmail(): Flow<String?> = context.dataStore.data.map { it[USER_EMAIL_KEY] }
    override fun getUserTier(): Flow<String> = context.dataStore.data.map { it[USER_TIER_KEY] ?: "free" }

    suspend fun isOnboardingComplete(): Boolean {
        return context.dataStore.data.first()[ONBOARDING_COMPLETE_KEY] ?: false
    }

    suspend fun setOnboardingComplete() {
        context.dataStore.edit { it[ONBOARDING_COMPLETE_KEY] = true }
    }

    private suspend fun saveAuthData(response: AuthResponse) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = response.token
            prefs[USER_ID_KEY] = response.userId
            prefs[USER_NAME_KEY] = response.name
            prefs[USER_EMAIL_KEY] = response.email
            prefs[USER_TIER_KEY] = response.subscriptionTier
        }
    }
}

@Singleton
class ResumeRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    @ApplicationContext private val context: Context
) : ResumeRepository {

    private suspend fun getToken(): String {
        val token = context.dataStore.data.first()[stringPreferencesKey("auth_token")] ?: throw IllegalStateException("Not authenticated")
        return "Bearer $token"
    }

    override suspend fun generateResume(request: GenerateResumeRequest): Resource<com.airesumebuilder.domain.repository.GenerateResumeResponse> {
        return try {
            val response = apiService.generateResume(
                getToken(),
                GenerateResumeDto(request.name, request.education, request.experience, request.skills, request.projects, request.certifications, request.template)
            )
            Resource.Success(com.airesumebuilder.domain.repository.GenerateResumeResponse(response.resume, response.resumeId))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to generate resume")
        }
    }

    override suspend fun generateCoverLetter(request: GenerateCoverLetterRequest): Resource<String> {
        return try {
            val response = apiService.generateCoverLetter(
                getToken(),
                GenerateCoverLetterDto(request.name, request.jobRole, request.company, request.jobDescription)
            )
            Resource.Success(response.coverLetter)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to generate cover letter")
        }
    }

    override suspend fun analyzeResume(resumeText: String): Resource<ResumeAnalysis> {
        return try {
            val response = apiService.analyzeResume(getToken(), AnalyzeResumeDto(resumeText))
            Resource.Success(ResumeAnalysis(response.suggestions, response.score))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to analyze resume")
        }
    }

    override suspend fun getResumes(): Resource<List<Resume>> {
        return try {
            val response = apiService.getResumes(getToken())
            Resource.Success(response.resumes.map { Resume(it.id, it.userId, it.title, it.content, it.template, it.createdAt, it.updatedAt) })
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load resumes")
        }
    }

    override suspend fun getResume(resumeId: String): Resource<Resume> {
        return try {
            val it = apiService.getResume(getToken(), resumeId)
            Resource.Success(Resume(it.id, it.userId, it.title, it.content, it.template, it.createdAt, it.updatedAt))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load resume")
        }
    }

    override suspend fun updateResume(resumeId: String, title: String, content: String, template: String): Resource<Resume> {
        return try {
            val it = apiService.updateResume(getToken(), resumeId, SaveResumeDto(title, content, template))
            Resource.Success(Resume(it.id, it.userId, it.title, it.content, it.template, it.createdAt, it.updatedAt))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update resume")
        }
    }

    override suspend fun deleteResume(resumeId: String): Resource<Unit> {
        return try {
            apiService.deleteResume(getToken(), resumeId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete resume")
        }
    }
}
