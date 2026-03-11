package org.delcom.watchlist.auth.model
import kotlinx.serialization.Serializable
@Serializable data class RegisterRequest(val name: String, val username: String, val password: String)
@Serializable data class LoginRequest(val username: String, val password: String)
@Serializable data class LogoutRequest(val authToken: String)
@Serializable data class RefreshRequest(val authToken: String, val refreshToken: String)
@Serializable data class RegisterResponse(val userId: String)
@Serializable data class LoginResponse(val authToken: String, val refreshToken: String)