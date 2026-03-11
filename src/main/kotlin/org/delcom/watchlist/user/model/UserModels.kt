package org.delcom.watchlist.user.model
import kotlinx.serialization.Serializable
@Serializable data class UserDto(val id: String, val name: String, val username: String, val about: String?, val createdAt: String, val updatedAt: String)
@Serializable data class UserResponse(val user: UserDto)
@Serializable data class UpdateProfileRequest(val name: String, val username: String)
@Serializable data class UpdatePasswordRequest(val password: String, val newPassword: String)
@Serializable data class UpdateAboutRequest(val about: String)