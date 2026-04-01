package com.airesumebuilder.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.util.*

object JwtConfig {
    private const val ISSUER = "ai-resume-builder"
    private const val AUDIENCE = "ai-resume-builder-users"
    private const val VALIDITY_MS = 7 * 24 * 60 * 60 * 1000L // 7 days

    private var secret: String = "ai-resume-builder-jwt-secret-change-in-production"

    fun init(config: ApplicationConfig) {
        secret = config.propertyOrNull("jwt.secret")?.getString() ?: secret
    }

    private val algorithm by lazy { Algorithm.HMAC256(secret) }

    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(ISSUER)
        .withAudience(AUDIENCE)
        .build()

    fun generateToken(userId: String, email: String): String = JWT.create()
        .withSubject("authentication")
        .withIssuer(ISSUER)
        .withAudience(AUDIENCE)
        .withClaim("userId", userId)
        .withClaim("email", email)
        .withExpiresAt(Date(System.currentTimeMillis() + VALIDITY_MS))
        .sign(algorithm)
}

fun Application.configureJWT(config: ApplicationConfig) {
    JwtConfig.init(config)
    install(Authentication) {
        jwt("auth-jwt") {
            realm = "ai-resume-builder"
            verifier(JwtConfig.verifier)
            validate { credential ->
                if (credential.payload.audience.contains("ai-resume-builder-users")) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}
