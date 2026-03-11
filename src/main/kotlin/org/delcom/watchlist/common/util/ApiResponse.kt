package org.delcom.watchlist.common.util
import kotlinx.serialization.Serializable
@Serializable data class ApiResponse<T>(val status: String, val message: String, val data: T? = null)
fun <T> success(message: String, data: T? = null) = ApiResponse(status = "success", message = message, data = data)
fun fail(message: String) = ApiResponse<Unit>(status = "fail", message = message, data = null)