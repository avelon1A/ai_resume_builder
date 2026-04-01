package com.airesumebuilder.models

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val userId: String,
    val email: String,
    val name: String,
    val subscriptionTier: String
)

@Serializable
data class ErrorResponse(
    val error: String,
    val code: Int = 400
)
